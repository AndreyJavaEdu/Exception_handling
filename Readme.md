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

![@ResponseStatus.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/Readme/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/ResponseStatusExceptionResolver/%40ResponseStatus.png)

Из недостатков такого подхода — как и в предыдущем случае отсутствует тело ответа. 
Но если нужно вернуть только код статуса, то @ResponseStatus довольно удобная штука.

<span style="color:#92d050">Кастомный HandlerExceptionResolver</span> позволит решить проблему 
из предыдущих примеров, наконец-то можно вернуть клиенту красивый JSON или XML с необходимой 
информацией. Но он сложный по реализации и используется старый класс для модели ModelAndView.

В качестве примера я сделал кастомный класс резолвер [ExceptionHandlingApplication.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2FExceptionHandlingApplication.java):
```java
package io.kamenskiy.situations.exception_handling.resolver;

import io.kamenskiy.situations.exception_handling.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
@Component
@Slf4j
public class CustomHandlerExceptionResolver extends AbstractHandlerExceptionResolver {
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // объект модели ModelAndView будет использовать `MappingJackson2JsonView` для отображения модели данных в формате JSON.
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        if (ex instanceof CustomException){
            modelAndView.setStatus(HttpStatus.BAD_REQUEST);
            modelAndView.addObject("message", "CustomException was handled");
            logger.error(ex.getMessage());
            return modelAndView;
        }
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("message", "Another Exception was handled");
        return modelAndView;
    }
}
```
Также создал свое исключение [CustomException.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fexception%2FCustomException.java):
```java
package io.kamenskiy.situations.exception_handling.exception;

public class CustomException extends Exception{
    public CustomException() {
    }

    public CustomException(String message) {
        super(message);
    }
}
```
А также реализовали контроллер [Example3Controller.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fcontroller%2FExample3Controller.java) для проверки кастомного резолвера:
```java
package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.CustomException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Example3Controller {
    @GetMapping(value = "/testCustomExceptionResolver", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testCustomHandlerExceptionResolver(@RequestParam(required = false, defaultValue = "false") boolean exception)
        throws CustomException {
        if(exception){
            throw new CustomException("CustomException in testCustomHandlerExceptionResolver");
        }
        return new Response("Ok");
    }
}
```
Приходится выполнять всю работу руками: сами проверяем тип исключения, и сами формируем объект 
древнего класса ModelAndView. На выходе конечно получим красивый JSON, 
но в коде красоты явно не хватает.

Такой резолвер может глобально перехватывать и обрабатывать любые типы исключений и 
возвращать как статус-код, так и тело ответа. Формально он даёт нам много возможностей и 
не имеет недостатков из предыдущих примеров.

Демонстрация вызова:

![Демонстрация вызова.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/Readme/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/%D0%9A%D0%B0%D1%81%D1%82%D0%BE%D0%BC%D0%BD%D1%8B%D0%B9%20%D1%80%D0%B5%D0%B7%D0%BE%D0%BB%D0%B2%D0%B5%D1%80%20CustomHandlerExceptionResolver/%D0%94%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D0%B2%D1%8B%D0%B7%D0%BE%D0%B2%D0%B0.png)

Видим что исключение прекрасно обработалось и в ответ получили код 400 и 
JSON с сообщением об ошибке.

### Обработка исключений с помощью @ControllerAdvice
Начиная со Spring 3.2 можно глобально и централизованно обрабатывать исключения с помощью классов 
с аннотацией @ControllerAdvice.

Для примера реализовали класс эдвайса - [DefaultAdvice.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fadvice%2FDefaultAdvice.java):
```java
package io.kamenskiy.situations.exception_handling.advice;

import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DefaultAdvice {
@ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response> handleException(BusinessException ex){
        Response response = new Response(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```
Итак мы создали в пакете advice класс DefaultAdvice, который проаннотировали аннотацией @ControllerAdvice.
Любой класс с аннотацией @ControllerAdvice будет являться глобальным обработчиком исключений, 
который очень гибко настраивается. В нашем случае мы создали класс DefaultAdvice с одним
единственным методом handleException(). Метод handleException() имеет аннотацию @ExceptionHandler,
в которой, можно определить список обрабатываемых исключений. 
В нашем случае будем перехватывать все исключения [BusinessException.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fexception%2FBusinessException.java).

Можно одним методом обрабатывать и несколько исключений сразу: 
@ExceptionHandler({BusinessException.class, ServiceException.class}). Так же можно в рамках эдвайса
сделать сразу несколько методов с аннотациями @ExceptionHandler для обработки разных исключений.  
Важное замечание, что метод handleException() возвращает объект ResponseEntity с нашим 
собственным типом Response (этот класс мы ранее реализовывали в пакете[dto](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fdto)).

Для проверки работы эдвайса я сделал простой контроллер:
```java
package io.kamenskiy.situations.exception_handling.controller;

import io.kamenskiy.situations.exception_handling.annotation.CustomExceptionHandler;
import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CustomExceptionHandler
public class Example5Controller {

    @GetMapping(value = "/testCustomAdvice", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response testCustomControllerAdvice(@RequestParam(required = false, defaultValue = "false") boolean exception)
            throws BusinessException{
        if (exception){
            throw new BusinessException("Была выброшена ошибка BusinessException в методе testCustomControllerAdvice");
        }
        return new Response("Все ОК");
    }
}
```
В результате, получаем красивый JSON в ответе пользователю и код 400:

![@ControllerAdvice.png](https://github.com/AndreyJavaEdu/Exception_handling/blob/Readme/%D0%A1%D1%85%D0%B5%D0%BC%D1%8B%20%D0%B8%20%D0%B4%D0%B5%D0%BC%D0%BE%D0%BD%D1%81%D1%82%D1%80%D0%B0%D1%86%D0%B8%D1%8F%20%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D1%8B%20Postman/ControllerAdvice/%40ControllerAdvice.png)


Если мы хотим обрабатывать исключения только от определенных контроллеров, то можно воспользоваться кастомной аннотацией.

Создали кастомный класс [CustomControllerAdvice.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fadvice%2FCustomControllerAdvice.java):
```java
package io.kamenskiy.situations.exception_handling.advice;

import io.kamenskiy.situations.exception_handling.annotation.CustomExceptionHandler;
import io.kamenskiy.situations.exception_handling.dto.Response;
import io.kamenskiy.situations.exception_handling.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice(annotations = CustomExceptionHandler.class)
public class CustomControllerAdvice {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response> handlerException(BusinessException ex){
        String message = String.format("%s %s", LocalDateTime.now(), ex.getMessage());
        Response response = new Response(message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```
Также создали аннотацию @CustomExceptionHandler - [CustomExceptionHandler.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fannotation%2FCustomExceptionHandler.java) в пакете [annotation](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fannotation):
```java
package io.kamenskiy.situations.exception_handling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomExceptionHandler {
}
```
В параметре аннотации @ControllerAdvice добавили атрибут annotations и присвоили ей нашу аннотацию.
Такая запись означает что CustomAdvice будет обрабатывать исключения только от тех контроллеров,
которые дополнительно имеют аннотацию @CustomExceptionHandler.
Исходный код контроллера - [Example6Controller.java](src%2Fmain%2Fjava%2Fio%2Fkamenskiy%2Fsituations%2Fexception_handling%2Fcontroller%2FExample6Controller.java):
```java
@RestController  
@CustomExceptionHandler  
public class Example5Controller {  
  
    @GetMapping(value = "/testCustomAdvice", produces = MediaType.APPLICATION_JSON_VALUE)  
    public Response testCustomControllerAdvice(@RequestParam(required = false, defaultValue = "false") boolean exception)  
        throws BusinessException{  
        if (exception){  
            throw new BusinessException("Была выброшена ошибка BusinessException в методе testCustomControllerAdvice");  
        }  
        return new Response("Все ОК");  
    }  
}
```
Для наглядности демонстрации мы изменили в методе сообщение об ошибке, стали добавлять к нему дату:
![Демонстрация CustomAdvice.png](%D1%F5%E5%EC%FB%20%E8%20%E4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F0%E0%E1%EE%F2%FB%20Postman%2FCustomAdvice%2F%C4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20CustomAdvice.png)


### Исключение ResponseStatusException.
Можно формировать ответ, путём выброса исключения ResponseStatusException:
```java
package io.kamenskiy.situations.exception_handling.controller;  
  
import io.kamenskiy.situations.exception_handling.dto.Response;  
import io.kamenskiy.situations.exception_handling.exception.BusinessException;  
import org.springframework.http.HttpStatus;  
import org.springframework.http.MediaType;  
import org.springframework.web.bind.annotation.GetMapping;  
import org.springframework.web.bind.annotation.RequestParam;  
import org.springframework.web.bind.annotation.RestController;  
import org.springframework.web.server.ResponseStatusException;  
  
@RestController  
public class Example6Controller {  
  
    @GetMapping(value = "/testResponseStatusException", produces = MediaType.APPLICATION_JSON_VALUE)  
    public Response testResponseStatusException(@RequestParam(required = false, defaultValue = "false") boolean exception)  
        throws BusinessException{  
        if (exception){  
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "ResponseStatusException in testResponseStatusException");  
        }  
        return new Response("Все ОК!!!");  
    }  
}
```
Выбрасывая ResponseStatusException можно также возвращать пользователю определённый код статуса, 
в зависимости от того, что произошло в логике приложения. При этом не нужно создавать кастомное 
исключение и прописывать аннотацию @ResponseStatus — просто выбрасываем исключение и передаём 
нужный статус-код.
Но тут возвращаемся к проблеме отсутствия тела сообщения, но в простых случаях такой подход 
может быть удобен.

![Демонстрация выброса исключения ResponseStatusException.png](%D1%F5%E5%EC%FB%20%E8%20%E4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%F0%E0%E1%EE%F2%FB%20Postman%2FResponseStatusException%2F%C4%E5%EC%EE%ED%F1%F2%F0%E0%F6%E8%FF%20%E2%FB%E1%F0%EE%F1%E0%20%E8%F1%EA%EB%FE%F7%E5%ED%E8%FF%20ResponseStatusException.png)
