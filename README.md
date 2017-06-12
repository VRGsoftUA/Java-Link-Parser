# Java Link Parser
### Here is our implimentation of Link Preview written in Java with RxJava2
## Usage
Initialize LinkCrawler
```java
LinkCrawler crawler = new LinkCrawler();
```
If you need to do somthing before parsing url, you can implement PreloadCallback
```java
public class MainActivity extends AppCompatActivity implements OnPreloadCallback

 crawler.setPreloadCallback(this);
```
To start parsing you need to use crawler.parseUrl and pass desired url, it returs ```Flowable<Result>```
```java
  crawler.parseUrl("https://github.com/")
                .subscribe(new Consumer<Result>() {
                    @Override
                    public void accept(@NonNull Result result) throws Exception {
                        mainBinding.setContent(result.getmParseContent());
                    }
                });
 ```
 Result object contains ParseContent field wich contains all parsed data of passed url, such as title,description etc. 
 #### [Kotlin version](https://github.com/VRGsoftUA/Kotlin-Link-Parser)
License
=================================

    Copyright 2016 VRG Soft

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
