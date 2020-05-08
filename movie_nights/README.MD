<h3>MongoDB Compass</h3>
<p>To login to MongoDB Atlas with MongoDB Compass copy the following URL:</p>
<ul>
    <li>mongodb+srv://admin:&lt;password&gt;@nights-1aruw.mongodb.net/test?retryWrites=true&w=majority</li>
    <li>Open MongoDB Compass and it should say you have a copied url use this, press YES</li>
    <li>Enter password: "movie-nights-password" and press connect</li>
</ul>

<h3>Custom Authentication</h3>
<p>Login request</p>
<ul>
    <li>Construct a json object, it must contain username and password property</li>
    <pre><code>
{ 
    "username":"your_username", 
    "password":"your_password" 
}
    </code></pre>
    <li>Send a POST request to "/api/auth/login</li>
    <li>Status codes:<ul>
        <li>If login is granted it will return status code: 200 and a object containing the keys JWT and USER,<br>
        a refresh token will be set in a cookie for you and you don't have to touch this at all.</li>
        <li>Refresh token is valid for 1 year, but can be blocked if needed</li>
        <li>If something is wrong with login it will return status code: 401 and message: "BAD CREDENTIALS"</li>
    </ul></li>
</ul>
<p>Refresh token</p>
<ul>
    <li>Send a POST request to "/api/auth/refresh_token with no body</li>
    <li>Status codes:<ul>
        <li>If refresh is granted it will return status code: 200 and a object containing the keys JWT and USER,<br>
        a refresh token will be set in a cookie for you and you don't have to touch this at all.</li>
        <li>Refresh token is valid for 1 year, but can be blocked if needed </li>
        <li>If something is wrong with refresh it will return status code: 401 and message: "UNAUTHORIZED"</li>
    </ul></li>
</ul>
<p>Get Active User</p>
<ul>
    <li>Send a GET request to "/api/auth/</li>
    <li>Status codes:<ul>
        <li>If the active dbUser(IN JWT) still exists in DB it will return status code: 200 and the dbUser object</li>
        <li>If the active dbUser(IN JWT) do not exist in DB it will return status code 204 and the message "Active dbUser do not exist"</li>
    </ul></li>
</ul>


<h3>Errors and Solutions</h3>
<ul>
    <li>
        java.lang.ClassNotFoundException: javax.xml.bind.DatatypeConverter
        <ul>
            <li>
                Caused By / Occurs when
                <ul>
                    <li>Can occur when trying to make a login request</li>
                    <li>Caused by something when creating a JWT</li>
                </ul>
            </li>
            <li>
                Solution
                    <ul>
                    <li>Define following dependency in pom.xml</li>
                        <li>
                        <pre><code>
 &lt;dependency>
     &lt;groupId>org.glassfish.jaxb&lt;/groupId>
     &lt;artifactId>jaxb-runtime&lt;/artifactId>
     &lt;version>2.3.2&lt;/version>
 &lt;/dependency>
                        </code></pre>
                        </li>
                    </ul>
            </li>
        </ul>
    </li>
    <li>Problem with "DELETE" and "PUT" mapping getting "FORBIDDEN"</li>
    <ul>
        <li>
        Solution
            <ul>
                <li>Probably you have a cors mapping setup like this.</li>
                <pre><code>
 @Configuration
 @EnableWebMvc
 public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**");
    }
 }
                </code></pre>
                <li>By default spring only allows "GET" and "POST" request, you need to allow the methods you want to use, like this </li>
                <pre><code>
 @Configuration
 @EnableWebMvc
 public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
       registry.addMapping("/**");
    }
 }
                 </code></pre>
            </ul>
        </li>
    </ul>
</ul>