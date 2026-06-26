# 极简天气

专为老年人设计的极简安卓天气 App，参考苹果天气设计风格，大字体、无广告、操作简单。

## 功能

- 🏠 自动定位当前城市天气
- 👆 右滑切换城市
- ➕ 添加新城市（支持拼音搜索和语音输入）
- 📊 24 小时逐时预报 + 降雨概率
- 📅 4 天预报
- 🎨 动态天气背景（晴天蓝天、雨天雨滴、雪天雪花）
- 💬 语音输入搜索城市

## 技术栈

Kotlin + Jetpack Compose + Retrofit + Coil

数据来源：和风天气 API（需自行注册）

## 构建

1. 在 https://console.qweather.com 注册并创建 JWT 凭据
2. 在项目根目录创建 `local.properties`，填入：

```properties
sdk.dir=你的Android SDK路径
qweatherKid=你的凭据ID
qweatherSub=你的项目ID
qweatherPrivateKey=你的私钥
qweatherApiHost=你的API域名
KEYSTORE_PATH=D:/AndroidAppSign/weather.jks
KEYSTORE_PASSWORD=密钥库密码
KEY_ALIAS=别名
KEY_PASSWORD=密钥密码
```

3. 生成签名密钥：

```bash
keytool -genkey -v -keystore weather.jks -alias weather -keyalg RSA -keysize 2048 -validity 10000
```

4. 构建：

```bash
./gradlew assembleRelease
```

## 下载

Release 页面下载最新 APK，直接安装使用。

## 截图

| 晴天 | 雨天 |
|------|------|
| 蓝色渐变背景 | 灰蓝背景 + 雨滴动画 |

## License

MIT
