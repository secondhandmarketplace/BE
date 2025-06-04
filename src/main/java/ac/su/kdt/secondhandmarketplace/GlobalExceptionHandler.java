
/**
 * @RestControllerAdvice와 @ExceptionHandler를 사용하여 전역적인 예외 처리를 구현할 수 있습니다.
 * 이렇게 하면 각 컨트롤러 메서드에서 try-catch 블록을 사용할 필요 없이 예외를 깔끔하게 처리할 수 있습니다.
 */
package ac.su.kdt.secondhandmarketplace;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //EntityNotFoundException (엔티티를 찾을 수 없을 때) 예외를 처리
    @ExceptionHandler(EntityNotFoundException.class) // EntityNotFoundException이 발생했을 때 이 메서드가 실행
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        // 클라이언트에게 "리소스를 찾을 수 없습니다: [에러 메시지]"와 404 Not Found 상태를 반환
        return new ResponseEntity<>("리소스를 찾을 수 없습니다: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(); // 에러 메시지를 담을 Map 객체를 생성
        ex.getBindingResult().getFieldErrors().forEach(error -> // 유효성 검증 실패한 각 필드에 대해 반복
                // 필드 이름과 에러 메시지를 Map에 추가
                errors.put(error.getField(), error.getDefaultMessage()));
        // 에러 상세 정보와 400 Bad Request 상태 코드를 반환
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllUncaughtException(Exception ex) {

        ex.printStackTrace(); // 실제 서비스에서는 로깅 프레임워크를 사용
        // 클라이언트에게 일반적인 에러 메시지와 500 Internal Server Error 상태를 반환
        return new ResponseEntity<>("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}