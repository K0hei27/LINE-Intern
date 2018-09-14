package com.proelbtn.linesc.controller

import com.proelbtn.linesc.annotation.Authentication
import com.proelbtn.linesc.model.UserGroupRelations
import com.proelbtn.linesc.model.UserGroups
import com.proelbtn.linesc.model.UserRelations
import com.proelbtn.linesc.request.CreateUserRequest
import com.proelbtn.linesc.response.UserResponse
import com.proelbtn.linesc.model.Users
import com.proelbtn.linesc.validator.validate_id
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class UsersController {
    @PostMapping(
            value = "/users",
            produces = [ APPLICATION_JSON_VALUE ]
    )
    @ApiOperation(
            value = "ユーザの作成用",
            notes = "ユーザを作成するのに使用するエンドポイント",
            response = UserResponse::class
    )
    @ApiResponses(
            value = [
                (ApiResponse( code = 200, message = "正常にユーザを作成できた。" )),
                (ApiResponse( code = 400, message = "引数が足りない・正しくない。"))
            ]
    )
    fun createUserInformation(
            @ApiParam(value = "作成したいユーザの情報") @RequestBody req: CreateUserRequest
                ): ResponseEntity<UserResponse> {
        var message: UserResponse? = null
        var status: HttpStatus = HttpStatus.OK

        // validate
        if (!req.validate()) status = HttpStatus.BAD_REQUEST

        // operation
        if (status == HttpStatus.OK) {
            val id = UUID.randomUUID()
            val sid = req.sid!!
            val name = req.name!!
            val pass = BCrypt.hashpw(req.pass!!, BCrypt.gensalt(8))
            val now = DateTime.now()

            transaction {
                // check if user is already registered
                val user = Users.select { Users.sid eq sid }.firstOrNull()
                if (user != null) status = HttpStatus.BAD_REQUEST

                // if user isn't registered, register it
                if (status == HttpStatus.OK) {
                    Users.insert {
                        it[Users.id] = id
                        it[Users.sid] = sid
                        it[Users.name] = name
                        it[Users.pass] = pass
                        it[Users.createdAt] = now
                        it[Users.updatedAt] = now
                    }
                }
            }

            if (status == HttpStatus.OK)
                message = UserResponse(id, sid, name, now.toString(), now.toString())
        }

        return ResponseEntity(message, status)
    }

    @GetMapping(
            value = "/users/{id}",
            produces = [ APPLICATION_JSON_VALUE ]
    )
    @ApiOperation(
            value = "ユーザの取得用",
            notes = "ユーザを取得するのに使用するエンドポイント",
            response = UserResponse::class
    )
    @ApiResponses(
            value = [
                (ApiResponse( code = 200, message = "正常にユーザを取得できた。" )),
                (ApiResponse( code = 400, message = "取得するユーザが存在しなかった。"))
            ]
    )
    fun getUserInformation(
            @ApiParam(value = "ユーザのID") @PathVariable("id") id: UUID
                ): ResponseEntity<UserResponse> {
        var message: UserResponse? = null
        var status: HttpStatus = HttpStatus.OK

        // operation
        val user = transaction { Users.select { Users.id eq id }.firstOrNull() }

        if (user == null) status = HttpStatus.NOT_FOUND
        else {
            message = UserResponse(
                    user[Users.id],
                    user[Users.sid],
                    user[Users.name],
                    user[Users.createdAt].toString(),
                    user[Users.updatedAt].toString()
            )
        }

        return ResponseEntity(message, status)
    }

    @Authentication
    @DeleteMapping(
            value = "/users/{id}",
            produces = [ APPLICATION_JSON_VALUE ]
    )
    @ApiOperation(
            value = "ユーザの削除用",
            notes = "ユーザを削除するのに使用するエンドポイント",
            response = UserResponse::class
    )
    @ApiResponses(
            value = [
                (ApiResponse( code = 200, message = "正常にユーザを削除できた。" )),
                (ApiResponse( code = 400, message = "引数が足りない・正しくない。")),
                (ApiResponse( code = 404, message = "削除するべきユーザが存在しなかった。"))
            ]
    )
    fun deleteUserInformation(
            @ApiParam(value = "認証されたユーザのID（トークンに含まれる）") @RequestAttribute("user") user: UUID,
            @ApiParam("ユーザのID") @PathVariable("id") id: UUID
                ): ResponseEntity<Unit> {
        var status: HttpStatus = HttpStatus.OK

        // validation
        if (user != id) status = HttpStatus.BAD_REQUEST

        // operation
        if (status == HttpStatus.OK) {
            val count = transaction {
                UserGroupRelations.deleteWhere { (UserGroupRelations.from eq id) or (UserGroupRelations.to eq id) }
                UserGroups.deleteWhere { UserGroups.owner eq id }
                UserRelations.deleteWhere { (UserRelations.from eq id) or (UserRelations.to eq id) }
                Users.deleteWhere { Users.id eq id }
            }

            if (count == 0) status = HttpStatus.NOT_FOUND
        }

        return ResponseEntity(status)
    }
}