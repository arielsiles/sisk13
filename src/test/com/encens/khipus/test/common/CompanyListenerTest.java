package com.encens.khipus.test.common;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.SystemFunction;
import com.encens.khipus.model.admin.SystemModule;
import com.encens.khipus.model.admin.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * JPA CompanyListener tests which ensures that all entities have the company relation properly configured.
 *
 * @author
 * @version 1.0.18
 */
public class CompanyListenerTest {

    private static final String MODEL_PACKAGE = "com.encens.khipus.model";
    private static final Class[] NO_COMPANY_RELATED_ENTITIES = {SystemFunction.class, SystemModule.class, Company.class};
    private List<Class> noCompanyAwareEntities;

    @BeforeClass
    public void init() {
        noCompanyAwareEntities = Arrays.asList(NO_COMPANY_RELATED_ENTITIES);
    }

    /**
     * Test if the set company method is working fine
     */
    @Test
    public void testSetter() {
        CompanyListener listener = new CompanyListener();
        User user = new User();
        Company currentCompany = new Company();
        listener.setCompany(user, currentCompany);
        Assert.assertNotNull(user.getCompany());
    }

    @Test
    public void checkAllEntitiesForADefinedCompanyFilter() throws Exception {
        List<Class> entitiesWithoutCompanyListener = new ArrayList<Class>();
        for (Class entityClass : getAllEntityClasses()) {
            if (!noCompanyAwareEntities.contains(entityClass)) {
                boolean isFound = false;
                for (Field field : entityClass.getDeclaredFields()) {
                    if (field.getName().equals("company") && field.isAnnotationPresent(ManyToOne.class)) {
                        isFound = true;
                        break;
                    }
                }
                if (!isFound) {
                    entitiesWithoutCompanyListener.add(entityClass);
                }
            }
        }
        Assert.assertTrue(entitiesWithoutCompanyListener.isEmpty(),
                "The following Entity classes have no the required company property and its relation, please fix and remember to follow the rules.\n" + entitiesWithoutCompanyListener);
    }

    /**
     * Returns only the classes that are entities
     *
     * @return list of classes
     * @throws Exception if something goes wrong
     */
    private static List<Class> getAllEntityClasses() throws Exception {
        List<Class> entities = new ArrayList<Class>();
        for (Class clazz : getClasses(MODEL_PACKAGE)) {
            if (clazz.isAnnotationPresent(Entity.class)) {
                entities.add(clazz);
            }
        }
        return entities;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException if class is not found
     * @throws IOException            file exception
     */
    private static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException if class is not found
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }


}
