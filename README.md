后台Media Service构想示例
---

最近客户一直想调用App的音乐播放器，但因为当初用的结构不适用，后面勉强另写一个服务来支持AIDL进程间控制～～～你懂的，总是会想要越来越多的支持。

这下干脆自身APP也用AIDL，本分享是一个参考系统Music的Demo样例

License
---

```
Copyright 2016 xuie0000

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```