### Обработка исключений в контроллерах Spring

Часто на практике возникает необходимость централизованной 
обработки исключений в рамках контроллера или даже всего приложения.

До Spring 3.2 основными способами обработки исключений в приложении 
были HandlerExceptionResolver и аннотация @ExceptionHandler.
Но они имеют определённые недостатки. Начиная с версии 3.2 появилась 
аннотация @ControllerAdvice_, в которой устранены ограничения из 
предыдущих решений. А в Spring 5 добавился новый класс 
ResponseStatusException, который очень удобен для обработки базовых 
ошибок для REST API.

Рассмотрим простой пет проект Exception_handling, в котором реализованы и показаны
раличные способы обработки исключений в контроллерах.

### Обработка исключений на уровне контроллера — @ExceptionHandler 
Контроллер - [Example1Controller.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fcontroller%2FExample1Controller.java):
```java
@RestController
public class Example1Controller {
    @GetMapping(value = "/testExceptionHandler", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testExceptionExceptionHandler(@RequestParam(required = false, defaultValue = "false") boolean exception)
            throws BusinessException {
        if(exception){
            throw new BusinessException("BusinessException in testExceptionExceptionHandler");
        }
        return new Response("Ok");
    }

    /**
     * Метод для обработки ошибок.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleException(BusinessException ex){
        return new Response(ex.getMessage());
    }
}
```
Класс DTO для преобразования ответа (response) из Java объекта
в вид JSON - [Response.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fdto%2FResponse.java):
```java
package io.kamenskiy.situations.exception_handling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String message;
}
```
В методе testExceptionExceptionHandler() контроллера мы обозначили, 
что метод производит (параметр producer аннотации @GetMapping) ответ в виде JSON.

Создали свой класс исключения и унаследовали его от класса Exception - [BusinessException.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fexception%2FBusinessException.java):
```java
package io.kamenskiy.situations.exception_handling.exception;

public class BusinessException extends Exception{
    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }
}
```
Метод в контроллере testExceptionExceptionHandler() реализован так, 
что он возвращает либо исключение BusinessException, либо успешный ответ — всё зависит 
от того что было передано в параметре запроса. Это нужно для того, чтобы можно было имитировать 
как штатную работу приложения, так и работу с ошибкой.

В контроллере также реализован метод handleException() проаннотированный @ExceptionHandler(BusinessException.class),
который необходим для обработки ошибок. Аннотация @ExceptionHandler говорит нам, что для последующей обработки
будут перехвачены все исключения типа BusinessException. В аннотации @ExceptionHandler можно 
прописать сразу несколько типов исключений.
Сама обработка исключения в данном случае примитивная и сделана просто для демонстрации работы метода — 
по сути вернётся код 200 и JSON с описанием ошибки.
если нужно вернуть другой код статуса, то можно воспользоваться 
дополнительно аннотацией @ResponseStatus, например @ResponseStatus(HttpStatus.BAD_REQUEST).

Демонстрация работы контроллера при обработке ошибки:

![Пример работы с ошибкой.png](%D1%F5%E5%EC%FB%20%E8%20%E4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F0%E0%E1%EE%F2%FB%20Postman%2F%40ExceptionHandler%2F%CF%F0%E8%EC%E5%F0%20%F0%E0%E1%EE%F2%FB%20%F1%20%EE%F8%E8%E1%EA%EE%E9.png)

Демонстрация штатной работы без, когда исключение не выбрасывается:

![Демонстрация штатной работы.png](%D1%F5%E5%EC%FB%20%E8%20%E4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F0%E0%E1%EE%F2%FB%20Postman%2F%40ExceptionHandler%2F%C4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F8%F2%E0%F2%ED%EE%E9%20%F0%E0%E1%EE%F2%FB.png)