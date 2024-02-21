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

Демонстрация штатной работы, когда исключение не выбрасывается:

![Демонстрация штатной работы.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/master/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/%40ExceptionHandler/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%88%D1%82%D0%B0%D1%82%D0%BD%D0%BE%D0%B9%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B.png)

<span style="color:#92d050">Основной недостаток</span> @ExceptionHandler в том что он определяется для каждого 
контроллера отдельно, а не глобально для всего приложения. Это ограничение можно обойти если @ExceptionHandler
определен в базовом классе, от которого будут наследоваться все контроллеры в приложении, 
но такой подход не всегда возможен.


### Обработка исключений с помощью HandlerExceptionResolver
<span style="color:#92d050">HandlerExceptionResolver</span> является общим интерфейсом для обработчиков 
исключений в Spring. Все исключения, выброшенные в приложении будут обработаны одним из подклассов 
HandlerExceptionResolver.  Можно сделать как свою собственную реализацию данного интерфейса, 
так и использовать существующие реализации, которые предоставляет нам Spring из коробки.

Из коробки Spring предлагает нам следующие реализации данного интерфейса:
- <span style="color:#92d050">ExceptionHandlerExceptionResolver</span> - этот резолвер является частью механизма 
обработки исключений с помощью аннотации @ExceptionHandler, который мы разобрали ранее.
- <span style="color:#92d050">DefaultHandlerExceptionResolver</span> — используется для обработки стандартных 
исключений Spring и устанавливает соответствующий код ответа, в зависимости от типа исключения:

| Exception | HTTP Status Code |
| ---- | ---- |
| BindException | 400 (Bad Request) |
| ConversionNotSupportedException | 500 (Internal Server Error) |
| HttpMediaTypeNotAcceptableException | 406 (Not Acceptable) |
| HttpMediaTypeNotSupportedException | 415 (Unsupported Media Type) |
| HttpMessageNotReadableException | 400 (Bad Request) |
| HttpMessageNotWritableException | 500 (Internal Server Error) |
| HttpRequestMethodNotSupportedException | 405 (Method Not Allowed) |
| MethodArgumentNotValidException | 400 (Bad Request) |
| MissingServletRequestParameterException | 400 (Bad Request) |
| MissingServletRequestPartException | 400 (Bad Request) |
| NoSuchRequestHandlingMethodException | 404 (Not Found) |
| TypeMismatchException | 400 (Bad Request) |

Основной недостаток заключается в том что возвращается только код статуса, тело ответа не возвращается.
Эту проблему можно решить с помощью ModelAndView, но это устаревший подход.

- <span style="color:#92d050">ResponseStatusExceptionResolver</span> — позволяет настроить код ответа для 
любого исключения с помощью аннотации @ResponseStatus.

В качестве примера я создал новый класс исключения [ServiceException.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fexception%2FServiceException.java):
```java
package io.kamenskiy.situations.exception_handling.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR) //будет возвращаться статус-код 500
public class ServiceException extends Exception{
    public ServiceException() {
    }
    public ServiceException(String message) {
        super(message);
    }
}
```
Для тестирования данного нового исключения я создал простой контроллер [Example2Controller.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fcontroller%2FExample2Controller.java):
```java
package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.ServiceException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example2Controller {
@GetMapping(value = "/testResponseStatusExceptionResolver", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testResponseStatusExceptionResolver(@RequestParam(required = false,  defaultValue = "true") boolean exception)
        throws ServiceException{
        if (exception){
            throw new ServiceException("ServiceException in method testResponseStatusExceptionResolver");
        }
        return new Response("Ok");
    }
}
```
Если отправить GET-запрос и передать параметр exception=true, то приложение в ответ вернёт 500-ю ошибку:

![@ResponseStatus.png](%D1%F5%E5%EC%FB%20%E8%20%E4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F0%E0%E1%EE%F2%FB%20Postman%2FResponseStatusExceptionResolver%2F%40ResponseStatus.png)

Из недостатков такого подхода — как и в предыдущем случае отсутствует тело ответа. 
Но если нужно вернуть только код статуса, то @ResponseStatus довольно удобная штука.