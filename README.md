<div align="center">
<h1>HiMiuiX</h1>

![stars](https://img.shields.io/github/stars/HChenX/HiMiuiX?style=flat)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/HiMiuiX)
![last commit](https://img.shields.io/github/last-commit/HChenX/HiMiuiX?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

[//]: # (<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>)
<p>仿 MiuiX 的 Preference Ui，xml式布局！</p>
</div>

### HiMiuiX

#### 包含:

- MiuiPreference
- MiuiAlertDialog
- MiuiDropDownPreference
- MiuiEditTextPreference
- MiuiSeekBarPreference
- MiuiSwitchPreference
- MiuiPreferenceCategory
- MiuiCardPreference
- 等.... 更多 Ui 正在构建ing

#### Demo 项目:

- [HiMiuiXDemo](https://github.com/HChenX/HiMiuiXDemo)
- 欢迎下载体验！

#### 展示:

- MiuiPreference
  ![MiuiPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiPreference.jpg)
  ![MiuiPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiPreference_dark.jpg)

- MiuiSwitchPreference
  ![MiuiSwitchPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSwitchPreference.jpg)
  ![MiuiSwitchPreference1](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSwitchPreference1.jpg)
  ![MiuiSwitchPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSwitchPreference_dark.jpg)

- MiuiDropDownPreference
  ![MiuiDropDownPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiDropDownPreference.jpg)
  ![MiuiDropDownPreference1](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiDropDownPreference1.jpg)
  ![MiuiDropDownPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiDropDownPreference_dark.jpg)

- MiuiEditTextPreference
  ![MiuiEditTextPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiEditTextPreference.jpg)
  ![MiuiEditTextPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiEditTextPreference_dark.jpg)

- MiuiAlertDialog
  ![MiuiAlertDialog_edit](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiAlertDialog_edit.jpg)
  ![MiuiAlertDialog_edit_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiAlertDialog_edit_dark.jpg)
  ![MiuiAlertDialog_items](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiAlertDialog_items.jpg)
  ![MiuiAlertDialog_items_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiAlertDialog_items_dark.jpg)

- MiuiSeekBarPreference
  ![MiuiSeekBarPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSeekBarPreference.jpg)
  ![MiuiSeekBarPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSeekBarPreference_dark.jpg)
  ![MiuiSeekBarPreference_dialog](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiSeekBarPreference_dialog.jpg)

- MiuiPreferenceCategory
  ![MiuiPreferenceCategory](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiPreferenceCategory.jpg)
  ![MiuiPreferenceCategory_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiPreferenceCategory_dark.jpg)

- MiuiCardPreference
  ![MiuiCardPreference](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiCardPreference.jpg)
  ![MiuiCardPreference_dark](https://raw.githubusercontent.com/HChenX/HiMiuiX/master/image/MiuiCardPreference_dark.jpg)

### 使用

- 请做为模块在项目中导入使用！

```shell
  # 在你仓库中执行，将本仓库作为模块使用
  git submodule add https://github.com/HChenX/HiMiuiX.git
  # 后续拉取本模块仓库
  git submodule update --init
```

- 然后设置项目 settings.gradle 添加:

```groovy
include ':HiMiuiX'
```

- 最后设置项目 app 下 build.gradle 文件，添加:

```groovy
implementation(project(':HiMiuiX'))
```

- tip: 请确保导入并使用了 `com.android.library`

#### 开源许可

- 本 UI 是对 MiuiX 的仿制而来，在此感谢 MiuiX！
- 本项目遵循 GPL-3.0 开源协议。
- 使用本项目请在项目中注明！
