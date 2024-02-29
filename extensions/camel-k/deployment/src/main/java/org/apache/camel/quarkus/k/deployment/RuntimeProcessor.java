/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.k.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.CamelModelReifierFactoryBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.CamelRuntimeTaskBuildItem;
import org.apache.camel.quarkus.core.deployment.spi.RuntimeCamelContextCustomizerBuildItem;
import org.apache.camel.quarkus.k.runtime.ApplicationRecorder;
import org.apache.camel.quarkus.k.runtime.ApplicationRoutes;
import org.apache.camel.quarkus.k.runtime.ApplicationRoutesConfig;
import org.apache.camel.quarkus.k.runtime.ApplicationShutdownConfig;
import org.apache.camel.spi.StreamCachingStrategy;
import org.jboss.jandex.IndexView;

import static org.apache.camel.quarkus.k.deployment.support.DeploymentSupport.getAllKnownImplementors;
import static org.apache.camel.quarkus.k.deployment.support.DeploymentSupport.reflectiveClassBuildItem;

public class RuntimeProcessor {
    @BuildStep
    void registerStreamCachingClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {

        final IndexView view = combinedIndex.getIndex();

        getAllKnownImplementors(view, StreamCachingStrategy.class)
                .forEach(i -> reflectiveClass.produce(reflectiveClassBuildItem(i)));

        getAllKnownImplementors(view, StreamCachingStrategy.Statistics.class)
                .forEach(i -> reflectiveClass.produce(reflectiveClassBuildItem(i)));

        getAllKnownImplementors(view, StreamCachingStrategy.SpoolRule.class)
                .forEach(i -> reflectiveClass.produce(reflectiveClassBuildItem(i)));
    }

    @BuildStep
    void unremovableBeans(
            BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<CamelRuntimeTaskBuildItem> runtimeTasks) {

        additionalBeans.produce(
                AdditionalBeanBuildItem.unremovableOf(ApplicationRoutes.class));
        additionalBeans.produce(
                AdditionalBeanBuildItem.unremovableOf(ApplicationRoutesConfig.class));
        additionalBeans.produce(
                AdditionalBeanBuildItem.unremovableOf(ApplicationShutdownConfig.class));

        runtimeTasks.produce(
                new CamelRuntimeTaskBuildItem("camel-k"));
    }

    @BuildStep
    @Record(value = ExecutionTime.STATIC_INIT, optional = true)
    CamelModelReifierFactoryBuildItem modelReifierFactory(ApplicationRecorder recorder) {
        return new CamelModelReifierFactoryBuildItem(recorder.modelReifierFactory());
    }

    @BuildStep
    @Record(value = ExecutionTime.RUNTIME_INIT)
    RuntimeCamelContextCustomizerBuildItem eventNotifier(ApplicationRecorder recorder, ApplicationShutdownConfig config) {
        return new RuntimeCamelContextCustomizerBuildItem(recorder.shutdownCustomizer(config));
    }
}
