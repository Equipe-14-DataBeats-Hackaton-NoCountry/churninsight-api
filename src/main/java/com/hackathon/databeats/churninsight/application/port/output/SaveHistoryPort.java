package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter.MySQLHistoryAdapter;

/**
 * Port de saída para persistência do histórico de predições.
 *
 * <p>Define o contrato para armazenamento de predições de churn com foco em auditoria.
 * Utiliza modelo de domínio ({@link PredictionHistory}) em vez de entidades JPA,
 * garantindo isolamento da camada de aplicação de detalhes de infraestrutura.</p>
 *
 * <p><b>Responsabilidade:</b> Persistir o resultado da predição com todos os metadados
 * necessários para rastreabilidade e análise posterior.</p>
 *
 * <p><b>Implementações:</b></p>
 * <ul>
 *   <li>{@link MySQLHistoryAdapter}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface SaveHistoryPort {

	/**
	 * Persiste um histórico de predição no banco de dados.
	 *
	 * @param history modelo de domínio contendo resultado da predição e metadados
	 * @throws com.hackathon.databeats.churninsight.infra.exception.ModelInferenceException
	 *         se ocorrer erro de persistência
	 */
	void save(PredictionHistory history);
}