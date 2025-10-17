

## Rule
- Always prefer to import a class/record instead of defining the type with the entire package name. e.g. 
    PREFER `HttpHeaders` instead of `org.springframework.http.HttpHeaders`
- Don't be overly defensive. excessive use of null checks and try catches are annoying