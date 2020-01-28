package com.sherepenko.android.logger

import com.sherepenko.android.archivarius.Archivarius

class ArchivariusLogger(
    archivarius: Archivarius,
    logContext: LogContext = emptyMap()
) : BaseLogger(ArchivariusLogWriter(archivarius), logContext)
