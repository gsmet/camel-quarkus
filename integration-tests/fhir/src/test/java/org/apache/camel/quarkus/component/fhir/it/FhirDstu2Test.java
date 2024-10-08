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
package org.apache.camel.quarkus.component.fhir.it;

import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.apache.camel.quarkus.component.fhir.it.util.Dstu2Enabled;
import org.apache.camel.quarkus.test.EnabledIf;

import static org.hamcrest.Matchers.is;

@QuarkusTest
@WithTestResource(value = FhirTestResource.class, initArgs = @ResourceArg(name = "fhirVersion", value = "DSTU2"))
@TestHTTPEndpoint(FhirDstu2Resource.class)
@EnabledIf(Dstu2Enabled.class)
class FhirDstu2Test extends AbstractFhirTest {

    public void capabilities(String encodeAs) {
        RestAssured.given()
                .queryParam("encodeAs", encodeAs)
                .get("/capabilities")
                .then()
                .statusCode(204);
    }

    public void metaGetFromServer() {
        RestAssured.given()
                .get("/meta/getFromServer")
                .then()
                .statusCode(200)
                .body(is("0"));
    }
}
