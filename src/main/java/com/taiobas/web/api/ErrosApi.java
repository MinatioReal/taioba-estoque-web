package com.taiobas.web.api;

import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ErrosApi {
    @ExceptionHandler({IllegalArgumentException.class,ArithmeticException.class})
    ResponseEntity<?> regra(RuntimeException e) { return ResponseEntity.badRequest().body(Map.of("mensagem",e instanceof ArithmeticException?"Quantidade excede o limite permitido.":e.getMessage())); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validacao(MethodArgumentNotValidException e) { return ResponseEntity.badRequest().body(Map.of("mensagem","Revise os campos informados.","campos",e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).toList())); }
    @ExceptionHandler(EmptyResultDataAccessException.class)
    ResponseEntity<?> ausente() { return ResponseEntity.status(404).body(Map.of("mensagem","Registro não encontrado.")); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflito() { return ResponseEntity.status(409).body(Map.of("mensagem","Registro duplicado ou vinculado a outros dados. Confira o CNPJ e os vínculos.")); }
}
