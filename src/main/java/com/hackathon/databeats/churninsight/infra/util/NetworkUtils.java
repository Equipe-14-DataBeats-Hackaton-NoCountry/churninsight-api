package com.hackathon.databeats.churninsight.infra.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Utilitário para operações de rede e extração de dados HTTP.
 *
 * <p>Fornece métodos auxiliares para análise de requisições HTTP, especialmente
 * útil em ambientes com proxies reversos e load balancers.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public class NetworkUtils {

	private NetworkUtils() {}

	/**
	 * Extrai o endereço IP real do cliente de uma requisição HTTP.
	 *
	 * <p><b>Estratégia de extração:</b></p>
	 * <ol>
	 *   <li>Verifica header {@code X-Forwarded-For} (proxy/CDN) - retorna primeiro IP da lista</li>
	 *   <li>Verifica header {@code X-Real-IP} (Nginx)</li>
	 *   <li>Retorna {@code RemoteAddr} da sessão (fallback)</li>
	 *   <li>Retorna "0.0.0.0" se requisição é null</li>
	 * </ol>
	 *
	 * <p><b>Nota:</b> Em ambientes com múltiplos proxies, {@code X-Forwarded-For} pode conter
	 * lista de IPs. Este método retorna o primeiro (cliente original).</p>
	 *
	 * @param request requisição HTTP
	 * @return endereço IP do cliente ou "0.0.0.0" se indisponível
	 */
	public static String getClientIp(HttpServletRequest request) {
		if (request == null) {
			return "0.0.0.0";
		}

		String ip = request.getHeader("X-Forwarded-For");

		if (StringUtils.hasText(ip)) {
			return ip.split(",")[0].trim();
		}

		ip = request.getHeader("X-Real-IP");
		if (StringUtils.hasText(ip)) {
			return ip;
		}

		return request.getRemoteAddr();
	}
}