# classification-tool

## Add your files

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/ee/gitlab-basics/add-file.html#add-a-file-using-the-command-line) or push an existing Git repository with the following command:

```
cd existing_repo
git remote add origin http://172.16.3.21/bigdata-dev/classification-tool.git
git branch -M main
git push -uf origin main
```

## 启动步骤

### step1:
    运行 TestV4类的main方法，以启动服务端
### step2:
    选择一个需要标记的原始语料文本，点击确认，即可加载原始语料至当前窗口
### step3:
    点击下方分类“标记”按钮，即可对原始语料进行分类标记

***

## 注意事项
### note1：
    resources目录下的classification.txt文件为需求中的分类标准，可根据实际情况进行修改

### note2：
    关闭当前服务窗口后，将对标记结果进行保存，依次保存至原始语料文本父目录target中

### note3：
    父目录target中将创建一个缓存文件cache~，以保存当前标记进度，下次启动时将自动加载
    
