package ca.pgx.common.processors

import java.util.Date

// TODO: check shell scripts and attrs of this class: date modified vs date changed - stat unix command, which
// one should we use?
case class AbstractFile(filename: String, sizeBytes: Long, dateModified: Date)