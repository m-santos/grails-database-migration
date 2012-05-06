/* Copyright 2010-2012 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.databasemigration

import grails.util.GrailsUtil
import liquibase.database.Database

import org.apache.log4j.Logger

/**
 * Based on the class of the same name from Mike Hugo's liquibase-runner plugin.
 *
 * @author Mike Hugo
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class MigrationRunner {

	private static Logger LOG = Logger.getLogger(this)

	static void autoRun() {

		if (!MigrationUtils.canAutoMigrate()) {
			return
		}

		def config = MigrationUtils.config

		if (!config.updateOnStart) {
			LOG.info "updateOnStart disabled; not running migrations"
			return
		}

		try {
			MigrationUtils.databases.each { String dsName, Database database ->
				MigrationUtils.executeInSession(dsName) {
					if (config.dropOnStart) {
						LOG.warn "Dropping tables..."
						MigrationUtils.getLiquibase(database).dropAll()
					}
					List updateOnStartFileNames = dsName == 'dataSource' ? config.updateOnStartFileNames : config."$dsName".updateOnStartFileNames
					for (String name in updateOnStartFileNames) {
						LOG.info "Running script '$name'"
						MigrationUtils.getLiquibase(database, name).update config.contexts
					}
				}
			}
		}
		catch (e) {
			GrailsUtil.deepSanitize e
			throw e
		}
	}
}
