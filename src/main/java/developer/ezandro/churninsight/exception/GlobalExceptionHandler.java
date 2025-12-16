package developer.ezandro.churninsight.exception;

import developer.ezandro.churninsigh.util.JsonFieldNameMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /* Erros de validação (@Valid)  */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::buildValidationMessage)
                .collect(Collectors.toList());

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                messages
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /* JSON inválido / malformado  */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "JSON inválido",
                List.of("O corpo da requisição está malformado ou inválido")
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /* Erro genérico (fallback) */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                List.of("Ocorreu um erro inesperado no servidor")
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* Mensagem customizada para campos inválidos */
    private String buildValidationMessage(FieldError error) {
        String jsonField = JsonFieldNameMapper.getJsonFieldName(error.getField());
        return "O campo '" + jsonField + "' é inválido ou obrigatório";
    }
}