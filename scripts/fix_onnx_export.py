"""
FIX: Exportar XGBoost para ONNX corretamente
Substitua a CÉLULA 9 do Colab por este código
"""

# CÉLULA 9 CORRIGIDA: Exportar para ONNX

# Instalar biblioteca correta
!pip install onnxmltools onnxruntime -q

import onnxmltools
from onnxmltools.convert.common.data_types import FloatTensorType

print("💾 Exportando XGBoost para ONNX...")

# Definir input shape
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]

# Converter XGBoost para ONNX
onx = onnxmltools.convert_xgboost(
    model, 
    initial_types=initial_type,
    target_opset=12
)

# Salvar
with open("modelo_xgboost.onnx", "wb") as f:
    f.write(onx.SerializeToString())

print("✅ Modelo exportado: modelo_xgboost.onnx")

# Testar se funciona
import onnxruntime as rt

sess = rt.InferenceSession("modelo_xgboost.onnx")
input_name = sess.get_inputs()[0].name

# Teste com uma amostra
test_sample = X_test.iloc[0:1].values.astype(np.float32)
pred_onnx = sess.run(None, {input_name: test_sample})

print(f"✅ Teste ONNX OK! Predição: {pred_onnx[1][0][1]:.4f}")

# Download
from google.colab import files
files.download('modelo_xgboost.onnx')
