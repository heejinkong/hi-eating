package kr.or.hieating.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Spring AI + 원격 Ollama 연동 설정 클래스. 이메일 "생성용"과 "검증용" 두 개의 ChatClient Bean을 각각 다른 원격 Ollama 서버로 구성
 * HTTP 연결/응답 타임아웃을 설정해 원격 서버 장애 시 무한 대기 방지
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@ConditionalOnProperty(
    prefix = "greenfood.ai",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AiConfig {

  @Bean("emailGenerationChatClient")
  public ChatClient emailGenerationChatClient(AiProperties properties) {
    return createChatClient(properties.generation());
  }

  @Bean("emailValidationChatClient")
  public ChatClient emailValidationChatClient(AiProperties properties) {
    return createChatClient(properties.validation());
  }

  /**
   * 공통 ChatClient 생성 로직. baseUrl, model, temperature, timeout 설정값으로 원격 Ollama 서버에 연결되는 ChatClient를
   * 만든다.
   *
   * @param settings 생성용 또는 검증용 Ollama 접속 설정
   * @return 설정이 반영된 ChatClient 인스턴스
   */
  private ChatClient createChatClient(AiProperties.Ollama settings) {
    // HTTP 요청 타임아웃 설정 (연결 타임아웃 / 응답 대기 타임아웃)
    // 원격 Ollama 서버 응답이 느리거나 죽었을 때 요청이 무한정 걸리지 않도록 방지
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(settings.connectTimeout()); // 커넥션 연결까지 대기 시간
    requestFactory.setReadTimeout(settings.readTimeout()); // 응답 수신까지 대기 시간

    // 위에서 만든 타임아웃 설정이 적용된 RestClient.Builder 구성
    RestClient.Builder restClientBuilder = RestClient.builder().requestFactory(requestFactory);

    // 원격 Ollama 서버 접속을 위한 API 클라이언트 생성
    // baseUrl: 접속할 서버 주소, restClientBuilder: 타임아웃이 반영된 HTTP 클라이언트 빌더 주입
    OllamaApi ollamaApi =
        OllamaApi.builder()
            .baseUrl(settings.baseUrl())
            .restClientBuilder(restClientBuilder)
            .build();

    // 모델명, temperature(응답의 창의성/일관성 정도) 등 요청 옵션 설정
    OllamaChatOptions options =
        OllamaChatOptions.builder()
            .model(settings.model())
            .temperature(settings.temperature())
            .build();

    // API + 옵션을 조합해 실제 호출 가능한 ChatModel 생성
    OllamaChatModel chatModel =
        OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();

    // ChatModel을 감싸서 prompt().user().call().content() 형태로 쓸 수 있는 ChatClient 반환
    return ChatClient.create(chatModel);
  }
}
