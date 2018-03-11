/*
 * Copyright 2017-2018 the original author or authors.
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

package com.uchuhimo.konf

import org.jetbrains.spek.subject.SubjectSpek

object RelocatedConfigSpek : SubjectSpek<Config>({

    subject { Prefix("network.buffer") + Config { addSpec(NetworkBuffer) }.at("network.buffer") }

    configSpek()
})

object RollUpConfigSpek : SubjectSpek<Config>({

    subject { Prefix("prefix") + Config { addSpec(NetworkBuffer) } }

    configSpek("prefix.network.buffer")
})

object DrillDownConfigSpek : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) }.at("network") }

    configSpek("buffer")
})