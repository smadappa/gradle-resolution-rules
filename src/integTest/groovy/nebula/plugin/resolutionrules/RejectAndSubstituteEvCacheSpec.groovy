/**
 *
 *  Copyright 2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nebula.plugin.resolutionrules

import spock.lang.Unroll

class RejectAndSubstituteEvCacheSpec extends AbstractRulesSpec {
    def setup() {
        buildFile << """\
            dependencies {
                resolutionRules files('${new File("src/main/resources/reject-and-substitute-evcache.json").absolutePath}')
            }
            """.stripIndent()
    }

    @Unroll
    def "substitution range for #declaredVersion"() {
        given:
        buildFile << """
            dependencies {
                compile "com.netflix.evcache:evcache-client:${declaredVersion}"
            }
            """.stripIndent()

        when:
        def result = runTasks('dependencies', '--configuration', 'compileClasspath')

        then:
        !result.output.contains("FAIL")
        result.output.contains(output)

        where:
        declaredVersion     | output
        '5.12.2'             | 'com.netflix.evcache:evcache-client:5.12.2 -> 5.11.2'
        '5.12.2'             | 'com.netflix.evcache:evcache-core:5.11.2'
        '5.12.1'             | 'com.netflix.evcache:evcache-client:5.12.1 -> 5.11.2'
        '5.12.0'             | 'com.netflix.evcache:evcache-client:5.12.0 -> 5.11.2'
        '5.+'                | 'com.netflix.evcache:evcache-client:5.+ -> 5.11.2' // a reject rule assists here
    }
}
