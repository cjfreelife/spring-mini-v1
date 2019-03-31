package org.cj.mvcframework.v1.servlet;

import org.cj.mvcframework.annotion.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class CjDispatcherServlet extends HttpServlet {
    private Map<String, Object> classNameMapping = new HashMap<String, Object>();
    private Map<String, Method> urlMethodMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        try {
            String requestURI = request.getRequestURI();
            Method method = urlMethodMapping.get(requestURI);
            if (method != null) {
                Class<?> declaringClass = method.getDeclaringClass();
                Map<String, String[]> parameterMap = request.getParameterMap();
                Parameter[] parameters = method.getParameters();
                List<Object> parameterNames = new ArrayList<>();
                for (Parameter parameter : parameters) {
                    CjParameter annotation = parameter.getAnnotation(CjParameter.class);
                    String value = annotation.value();
                    if (!"".equals(parameterMap.get(value)[0])) {
                        Class<?> type = parameter.getType();
                        parameterNames.add(type.getConstructor(String.class).newInstance(parameterMap.get(value)[0]));
                    }
                }
                Object[] objects = new Object[parameterNames.size()];
                for (int i = 0; i < parameterNames.size(); i++) {
                    objects[i] = parameterNames.get(i);
                }
                Object o = classNameMapping.get(declaringClass.getName());
                method.invoke(o, objects);
            }
        } catch (Exception e) {
            List<StackTraceElement> stackTraceElements = Arrays.asList(e.getStackTrace());
            stackTraceElements.forEach(obj -> {
                try {
                    response.getWriter().write(String.valueOf(obj));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        }

    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.读取配置文件
        //2.扫描配置包路径下所有文件
        //3.获取配置的
        Properties configContext = new Properties();
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
        try {
            configContext.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String scanPackage = configContext.getProperty("scanPackage");
        doScanner(scanPackage);

        for (String className : classNameMapping.keySet()) {
            Class<?> clz = null;
            try {
                clz = Class.forName(className);
                //如果类里标记了controller
                if (clz.isAnnotationPresent(CjService.class)) {
                    CjService annotation = clz.getAnnotation(CjService.class);
                    String beanName = annotation.value();
                    Object o = clz.newInstance();
                    classNameMapping.put(className, o);
                    if (!"".equals(beanName)) {
                        className = beanName;
                    }
                    classNameMapping.put(className, o);
                    Class<?>[] interfaces = clz.getInterfaces();
                    for (Class<?> anInterface : interfaces) {
                        classNameMapping.put(anInterface.getName(), o);
                    }
                } else if (clz.isAnnotationPresent(CjController.class)) {
                    CjController annotation = clz.getAnnotation(CjController.class);
                    String beanName = annotation.value();
                    if ("".equals(beanName)) {
                        beanName = className;
                    }
                    classNameMapping.put(beanName, clz.newInstance());
                    if (clz.isAnnotationPresent(CjRequestMapping.class)) {
                        CjRequestMapping cjRequestMapping = clz.getAnnotation(CjRequestMapping.class);
                        String baseUrl = "";
                        if (!"".equals(cjRequestMapping.value())) {
                            baseUrl = cjRequestMapping.value();
                        }
                        Method[] methods = clz.getMethods();
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(CjRequestMapping.class)) {
                                CjRequestMapping methodAnnotation = method.getAnnotation(CjRequestMapping.class);
                                if (!"".equals(methodAnnotation.value())) {
                                    urlMethodMapping.put("/" + baseUrl + "/" + methodAnnotation.value(), method);
                                }
                            }
                        }
                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Object object : classNameMapping.values()) {
            if (object == null) {
                continue;
            }
            Class<?> clz = object.getClass();
            try {
                if (clz.isAnnotationPresent(CjController.class)) {
                    Field[] fields = clz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(CjAutowired.class)) {
                            CjAutowired annotation = field.getAnnotation(CjAutowired.class);
                            String name = field.getName();
                            field.setAccessible(true);
                            field.set(classNameMapping.get(clz.getName()), classNameMapping.get(name));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(urlMethodMapping);
        System.out.println(classNameMapping);
    }

    //scanPackage : com.cj.demo
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File[] files = new File(url.getFile()).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println(file);
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                classNameMapping.put(scanPackage + "." + file.getName().replace(".class", ""), null);
            }
        }
    }
}
