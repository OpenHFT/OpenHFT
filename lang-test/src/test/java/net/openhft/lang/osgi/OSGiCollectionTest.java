/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.osgi;

import net.openhft.lang.collection.HugeArray;
import net.openhft.lang.collection.HugeCollections;
import net.openhft.langosgi.model.JavaBeanInterface;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * @author lburgazzoli
 */
@RunWith(PaxExam.class)
public class OSGiCollectionTest extends OSGiTestBase {
    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        return options(
            systemProperty("org.osgi.framework.storage.clean").value("true"),
            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            mavenBundleAsInProject("org.slf4j","slf4j-api"),
            mavenBundleAsInProject("org.slf4j","slf4j-simple").noStart(),
            mavenBundleAsInProject("net.openhft","affinity"),
            mavenBundleAsInProject("net.openhft","compiler"),
            mavenBundleAsInProject("net.openhft","collections"),
            mavenBundleAsInProject("net.openhft","lang"),
            workspaceBundle("lang-test"),
            junitBundles(),
            systemPackage("sun.misc"),
            systemPackage("sun.nio.ch"),
            systemPackage("com.sun.jna"),
            systemPackage("com.sun.jna.ptr"),
            systemPackage("com.sun.tools.javac.api"),
            cleanCaches()
        );
    }

    @Test
    @Ignore
    public void checkHugeArray() {
        int length = 10 * 1000 * 1000;
        HugeArray<JavaBeanInterface> array = HugeCollections.newArray(JavaBeanInterface.class, length);

        assertNotNull(array);
    }
}
