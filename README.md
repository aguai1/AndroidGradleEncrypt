### 简书地址：https://www.jianshu.com/p/c68e08069460
### GitHub地址：https://github.com/aguai1/AndroidGradleEncrypt

#### 本文将介绍android studio利用gradle进行raw资源文件加密的一种方式（所有代码均写在build.gradle中）：
####

##### 第一步：首先要需要了解的是gradle打包监听器BuildListener的两个方法：
 * projectsEvaluated（）方法执行在生成apk前： 所以在projectsEvaluated方法进行apk资源文件copy工作和加密工作。
* buildFinished（）方法执行在apk生成之后： 在buildFinished方法中进行加密资源文件的恢复和临时文件的删除工作（临时文件用于未加密代码的临时存储，打包结束后需要放会raw文件夹中）
* 需copy如下代码：
	```
	gradle.addBuildListener(new BuildListener() {
	    @Override
	    void buildStarted(Gradle gradle) {
	        println "buildStarted"
	    }
	
	    @Override
	    void settingsEvaluated(Settings settings) {
	        println "settingsEvaluated"
	
	    }
	
	    @Override
	    void projectsLoaded(Gradle gradle) {
	        println "projectsLoaded"
	
	    }
	
	    @Override
	    void projectsEvaluated(Gradle gradle) {
	        println "projectsEvaluated"
	        copyFolder(rawDir,tempDir)
	        encodeDir(rawDir,aesKeyCommen)
	    }
	    @Override
	    void buildFinished(BuildResult buildResult) {
	        copyFolder(tempDir,rawDir);
	        deleteAllFilesOfDir(tempDir);
	    }
	})
	```


##### 第二步：在build.gradle中实现上步骤的方法copyFolder，encodeDir，deleteAllFileofDir
#####
gradle打包兼容java代码，下面贴出这些方法的实现：

```
	//加密的资源文件路径
	def rawDir ='./app/src/main/res/raw/'
	//资源文件临时存储文件夹名称
	def tempDir ='./tempDir'
	//加密的key
	def aesKey = "\"abcdefgabcdefg12\""
	
	def aesKeyCommen = "abcdefgabcdefg12"
	
	//拷贝文件夹
	void copyFolder(String oldPath, String newPath) {
	    try {
	        (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
	        File a = new File(oldPath);
	        String[] file = a.list();
	        File temp = null;
	        for (int i = 0; i < file.length; i++) {
	            if (oldPath.endsWith(File.separator)) {
	                temp = new File(oldPath + file[i]);
	            } else {
	                temp = new File(oldPath + File.separator + file[i]);
	            }
	            if (temp.isFile()) {
	                FileInputStream input = new FileInputStream(temp);
	                FileOutputStream output = new FileOutputStream(newPath + "/" +
	                        (temp.getName()).toString());
	                byte[] b = new byte[1024 * 5];
	                int len;
	                while ((len = input.read(b)) != -1) {
	                    output.write(b, 0, len);
	                }
	                output.flush();
	                output.close();
	                input.close();
	            }
	            if (temp.isDirectory()) {//如果是子文件夹
	                copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
	            }
	        }
	    }
	    catch (Exception e) {
	        println("复制整个文件夹内容操作出错");
	        e.printStackTrace();
	    }
	}
	
	//删除文件夹
	void deleteAllFilesOfDir(String path) {
	    File file=new File(path);
	    if (!file.exists())
	        return;
	    if (file.isFile()) {
	        file.delete();
	        return;
	    }
	    File[] files = file.listFiles();
	    for (int i = 0; i < files.length; i++) {
	        deleteAllFilesOfDir(files[i].getAbsolutePath());
	    }
	    file.delete();
	}
	
	//读取文件到string
	static String file2String(File file, String encoding) {
	    InputStreamReader reader = null;
	    StringWriter writer = new StringWriter();
	    try {
	        if (encoding == null || "".equals(encoding.trim())) {
	            reader = new InputStreamReader(new FileInputStream(file), encoding);
	        } else {
	            reader = new InputStreamReader(new FileInputStream(file));
	        }
	        //将输入流写入输出流
	        char[] buffer = new char[1024];
	        int n = 0;
	        while (-1 != (n = reader.read(buffer))) {
	            writer.write(buffer, 0, n);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (reader != null)
	            try {
	                reader.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	    }
	    //返回转换结果
	    if (writer != null)
	        return writer.toString();
	    else return null;
	}
	//加密算法
	private static byte[] encrypt(String content, String password) {
	    try {
	        byte[] keyStr = getKey(password);
	        SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
	        byte[] byteContent = content.getBytes("utf-8");
	        cipher.init(Cipher.ENCRYPT_MODE, key);//   ʼ
	        byte[] result = cipher.doFinal(byteContent);
	        return result; //
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	//解密算法
	private static byte[] decrypt(byte[] content, String password) {
	    try {
	        byte[] keyStr = getKey(password);
	        SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
	        cipher.init(Cipher.DECRYPT_MODE, key);//   ʼ
	        byte[] result = cipher.doFinal(content);
	        return result; //
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	private static byte[] getKey(String password) {
	    byte[] rByte = null;
	    if (password!=null) {
	        rByte = password.getBytes();
	    }else{
	        rByte = new byte[24];
	    }
	    return rByte;
	}
	
	//二进制转16进制
	static String parseByte2HexStr(byte[] buf) {
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < buf.length; i++) {
	        String hex = Integer.toHexString(buf[i] & 0xFF);
	        if (hex.length() == 1) {
	            hex = '0' + hex;
	        }
	        sb.append(hex.toUpperCase());
	    }
	    return sb.toString();
	}
	
	static byte[] parseHexStr2Byte(String hexStr) {
	    if (hexStr.length() < 1)
	        return null;
	    byte[] result = new byte[hexStr.length() / 2];
	    for (int i = 0; i < hexStr.length() / 2; i++) {
	        int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
	        int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
	                16);
	        result[i] = (byte) (high * 16 + low);
	    }
	    return result;
	}
	
	//加密方法
	static String encodeContent(String content, String keyBytes){
	    //加密之后的字节数组,转成16进制的字符串形式输出
	    return parseByte2HexStr(encrypt(content, keyBytes));
	}
	
	//解密方法
	static String decodeContent(String content, String keyBytes){
	    //解密之前,先将输入的字符串按照16进制转成二进制的字节数组,作为待解密的内容输入
	    byte[] b = decrypt(parseHexStr2Byte(content), keyBytes);
	    return new String(b);
	}
	
	//projectsEvaluated里调用的方法  aesKey： key  rawDir：加密的文件夹
	void encodeDir(String rawDir, String aesKey){
	    println "do 加密代码"
	    File searchPlug = new File(rawDir);
	    if (searchPlug.exists() && searchPlug.isDirectory()) {
	        print "文件夹存在"
	        File[] files = searchPlug.listFiles()
	        for (File file : files) {
	            if(!file.name.endsWith(".glsl")){
	                continue
	            }
	            String str=file2String(file,"utf-8")
	            def content = encodeContent(str, aesKey)
	//            def result = decodeContent(content, aesKey)
	//            println(" 原始文件:content"+str)
	//            println(" 加密后文件:content"+content)
	//            println(" 解密后文件:content"+result)
	            def stream = file.newOutputStream()
	            stream.write(content.bytes)
	            stream.flush()
	        }
	    }
	}
```
	
	


#####第三步：在gradle和android程序中实现加密key的共享
#####
	
1.   在gradle文件中增加代码

```
		defaultConfig {
			 buildConfigField "String", "AES_KEY",aesKey
	        }
```

2.  在android代码中利用BuildConfig.AES_KEY获取build.gradle文件中配置的key。
3.  注：在gradle文件中使用的key用def aesKeyCommen = "abcdefgabcdefg12"
		在 buildConfigField 里需要传入的String为： "\"abcdefgabcdefg12\""
		因为gradle生成java类时会默认省掉“”。 
#####第四步：android程序里解密 #####
1 . 新建工具类：
```
public class AESUtils {


    private static byte[] encrypt(String content, String password) {
        try {
            byte[] keyStr = getKey(password);
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);//   ʼ
            byte[] result = cipher.doFinal(byteContent);
            return result; //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decrypt(byte[] content, String password) {
        try {
            byte[] keyStr = getKey(password);
            SecretKeySpec key = new SecretKeySpec(keyStr, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");//algorithmStr
            cipher.init(Cipher.DECRYPT_MODE, key);//   ʼ
            byte[] result = cipher.doFinal(content);
            return result; //
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getKey(String password) {
        byte[] rByte = null;
        if (password!=null) {
            rByte = password.getBytes();
        }else{
            rByte = new byte[24];
        }
        return rByte;
    }

    /**
     * 将二进制转换成16进制
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     *加密
     */
    public static String encode(String content,String keyBytes){
        //加密之后的字节数组,转成16进制的字符串形式输出
        return parseByte2HexStr(encrypt(content, keyBytes));
    }

    /**
     *解密
     */
    public static String decode(String content,String keyBytes){
        //解密之前,先将输入的字符串按照16进制转成二进制的字节数组,作为待解密的内容输入
        byte[] b = decrypt(parseHexStr2Byte(content), keyBytes);
        return new String(b);
    }

   

}
```

2 . 读取raw资源文件，并进行解密

```
public static String readShaderFromRawResource(final int resourceId) {
        final InputStream inputStream = CameraApplication.getInstance().getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            final StringBuilder body = new StringBuilder();

            try {
                while ((nextLine = bufferedReader.readLine()) != null) {
                    body.append(nextLine);
                    body.append('\n');
                }
            } catch (IOException e) {
                return null;
            }
            //AESUtils.decode是解密方法，BuildConfig.AES_KEY，为gradle中配置的key
            String decrypt = AESUtils.decode(body.toString(), BuildConfig.AES_KEY);
            return decrypt;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";

    }

```
#####第五步：大功告成，解压生成的apk，然后查看raw资源文件 #####
可看到文件内容为3134141425425 的十六进制构成，而代码中的raw文件还是原来的代码，没有发生任何变化

