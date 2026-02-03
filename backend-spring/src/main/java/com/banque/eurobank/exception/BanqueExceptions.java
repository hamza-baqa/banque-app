package com.banque.eurobank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceptions métier pour l'application bancaire EuroBank
 */

// ==================== EXCEPTIONS CLIENT ====================

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS COMPTE ====================

@ResponseStatus(HttpStatus.NOT_FOUND)
class CompteNotFoundException extends RuntimeException {
    public CompteNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class SoldeInsuffisantException extends RuntimeException {
    public SoldeInsuffisantException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class CompteInactifException extends RuntimeException {
    public CompteInactifException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class CompteBloqueException extends RuntimeException {
    public CompteBloqueException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS VIREMENT ====================

@ResponseStatus(HttpStatus.BAD_REQUEST)
class VirementInvalideException extends RuntimeException {
    public VirementInvalideException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class LimiteDepasseeException extends RuntimeException {
    public LimiteDepasseeException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class IbanInvalideException extends RuntimeException {
    public IbanInvalideException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS CARTE ====================

@ResponseStatus(HttpStatus.NOT_FOUND)
class CarteNotFoundException extends RuntimeException {
    public CarteNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class CarteBloqueException extends RuntimeException {
    public CarteBloqueException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class PlafondDepasseException extends RuntimeException {
    public PlafondDepasseException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS AUTHENTIFICATION ====================

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class CompteVerrouilleException extends RuntimeException {
    public CompteVerrouilleException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class TokenExpireException extends RuntimeException {
    public TokenExpireException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class TokenInvalideException extends RuntimeException {
    public TokenInvalideException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS TECHNIQUES ====================

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class TechnicalException extends RuntimeException {
    public TechnicalException(String message) {
        super(message);
    }
    
    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ServiceIndisponibleException extends RuntimeException {
    public ServiceIndisponibleException(String message) {
        super(message);
    }
}

// ==================== EXCEPTIONS CONFORMITÉ ====================

@ResponseStatus(HttpStatus.FORBIDDEN)
class OperationNonAutoriseeException extends RuntimeException {
    public OperationNonAutoriseeException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ValidationKYCException extends RuntimeException {
    public ValidationKYCException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class OperationSuspicieuse extends RuntimeException {
    public OperationSuspicieuse(String message) {
        super(message);
    }
}
