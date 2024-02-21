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

![Пример работы с ошибкой.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/master/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/%40ExceptionHandler/%D0%9F%D1%80%D0%B8%D0%BC%D0%B5%D1%80%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20%D1%81%20%D0%BE%D1%88%D0%B8%D0%B1%D0%BA%D0%BE%D0%B9.png)

Демонстрация штатной работы без, когда исключение не выбрасывается:

![Демонстрация штатной работы.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/master/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/%40ExceptionHandler/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%88%D1%82%D0%B0%D1%82%D0%BD%D0%BE%D0%B9%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B.png)