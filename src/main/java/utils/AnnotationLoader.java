package utils;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AnnotationLoader {

    public static List<Class<?>> getAllClassFromInterface(Class c){
        List<Class<?>> returnClassList = null;

        if(c.isInterface()){
            String packageName = c.getPackage().getName();
            List<Class<?>> allClass = getClasses(packageName);
            if(allClass != null){
                returnClassList = new ArrayList<>();
                for(Class classes : allClass){
                    if(c.isAssignableFrom(classes)){
                        if(!c.equals(classes)){
                            returnClassList.add(classes);
                        }
                    }
                }
            }
        }
        return returnClassList;
    }

    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }


    public static List<Class<?>> getClasses(String packageName){

        List<Class<?>> classes = new ArrayList<>();

        boolean recursive = true;

        String packageDirName = packageName.replace('.','/');

        Enumeration<URL> dirs;

        try{
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);

            while(dirs.hasMoreElements()){
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if("file".equalsIgnoreCase(protocol)){
                    String filePath = URLDecoder.decode(url.getFile(),"UTF-8");
                    findAndAddClassesInPackageByFile(packageName,filePath,recursive,classes);
                }else if("jar".equalsIgnoreCase(protocol)){

                    try(JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile()){
                        Enumeration<JarEntry> entries = jar.entries();
                        while(entries.hasMoreElements()){

                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if(name.charAt(0) == '/'){
                                name = name.substring(1);
                            }
                            if(name.startsWith(packageDirName)){
                                int idx = name.lastIndexOf('/');

                                if(idx != -1){
                                    packageName = name.substring(0,idx).replace('/','.');
                                }

                                if((idx != -1) || recursive){
                                    if(name.endsWith(".class") && !entry.isDirectory()){
                                        String className = name.substring(packageName.length() + 1,name.length() - 6);
                                        try{
                                            classes.add(Class.forName(packageName + "." + className));
                                        }catch (ClassNotFoundException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return classes;
    }

    public static void findAndAddClassesInPackageByFile(String packageName,String packagePath,final boolean recursive,
                                                        List<Class<?>> classes){
        File dir = new File(packagePath);

        if(!dir.exists() || !dir.isDirectory()){
            return;
        }

        File[] dirfiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (recursive && file.isDirectory() || (file.getName().endsWith(".class")));
            }
        });

        for(File file : dirfiles){

            if(file.isDirectory()){
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),recursive,classes);
            }else{
                //如果是java类文件，去掉后面的.class 只留类名
                String className = file.getName().substring(0,file.getName().length()-6);
                try{
                    classes.add(Class.forName(packageName + "." +className));
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
