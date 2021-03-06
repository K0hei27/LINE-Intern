package com.proelbtn.linesc.model

import org.jetbrains.exposed.sql.Table

object UserGroupMessages: Table() {
    val id = uuid("id").primaryKey()
    val from = (uuid("from") references Users.id).index()
    val to = (uuid("to") references UserGroups.id).index()
    val content = varchar("content", 1024)
    val createdAt = datetime("created_at")
}