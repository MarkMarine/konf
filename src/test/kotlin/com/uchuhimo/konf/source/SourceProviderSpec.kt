/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.uchuhimo.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import com.uchuhimo.konf.source.properties.PropertiesProvider
import com.uchuhimo.konf.tempFileOf
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import spark.Spark.get
import spark.Spark.stop
import java.net.ConnectException
import java.net.URL

object SourceProviderSpec : SubjectSpek<SourceProvider>({
    subject { PropertiesProvider }

    given("a source provider") {
        on("create source from reader") {
            val source = subject.fromReader("type = reader".reader())
            it("should return a source which contains value from reader") {
                assertThat(source["type"].toText(), equalTo("reader"))
            }
        }
        on("create source from input stream") {
            val source = subject.fromInputStream(
                    tempFileOf("type = inputStream").inputStream())
            it("should return a source which contains value from input stream") {
                assertThat(source["type"].toText(), equalTo("inputStream"))
            }
        }
        on("create source from file") {
            val file = tempFileOf("type = file")
            val source = subject.fromFile(file)
            it("should create from the specified file") {
                assertThat(source.context["file"], equalTo(file.toString()))
            }
            it("should return a source which contains value in file") {
                assertThat(source["type"].toText(), equalTo("file"))
            }
        }
        on("create source from string") {
            val content = "type = string"
            val source = subject.fromString(content)
            it("should create from the specified string") {
                assertThat(source.context["content"], equalTo("\"\n$content\n\""))
            }
            it("should return a source which contains value in string") {
                assertThat(source["type"].toText(), equalTo("string"))
            }
        }
        on("create source from byte array") {
            val source = subject.fromBytes("type = bytes".toByteArray())
            it("should return a source which contains value in byte array") {
                assertThat(source["type"].toText(), equalTo("bytes"))
            }
        }
        on("create source from byte array slice") {
            val source = subject.fromBytes("|type = slice|".toByteArray(), 1, 12)
            it("should return a source which contains value in byte array slice") {
                assertThat(source["type"].toText(), equalTo("slice"))
            }
        }
        on("create source from HTTP URL") {
            get("/source") { _, _ -> "type = http" }
            val urlPath = "http://localhost:4567/source"
            var source: Source? = null
            // wait until server ready
            for (x in 1..10000) {
                try {
                    source = subject.fromUrl(URL(urlPath))
                    println("wait for $x ms when testing source provider")
                    break
                } catch (e: ConnectException) {
                    Thread.sleep(1)
                    continue
                }
            }
            it("should create from the specified URL") {
                assertThat(source!!.context["url"], equalTo(urlPath))
            }
            it("should return a source which contains value in URL") {
                assertThat(source!!["type"].toText(), equalTo("http"))
            }
            stop()
        }
        on("create source from file URL") {
            val file = tempFileOf("type = fileUrl")
            val url = file.toURI().toURL()
            val source = subject.fromUrl(url)
            it("should create from the specified URL") {
                assertThat(source.context["url"], equalTo(url.toString()))
            }
            it("should return a source which contains value in URL") {
                assertThat(source["type"].toText(), equalTo("fileUrl"))
            }
        }
        on("create source from resource") {
            val resource = "source/provider.properties"
            val source = subject.fromResource(resource)
            it("should create from the specified resource") {
                assertThat(source.context["resource"], equalTo(resource))
            }
            it("should return a source which contains value in resource") {
                assertThat(source["type"].toText(), equalTo("resource"))
            }
        }
        on("create source from non-existed resource") {
            it("should throw SourceNotFoundException") {
                assertThat({ subject.fromResource("source/no-provider.properties") },
                        throws<SourceNotFoundException>())
            }
        }
    }
})
