/*
 Navicat Premium Data Transfer

 Source Server         : 本地mongo
 Source Server Type    : MongoDB
 Source Server Version : 80013 (8.0.13)
 Source Host           : localhost:27017
 Source Schema         : eugene

 Target Server Type    : MongoDB
 Target Server Version : 80013 (8.0.13)
 File Encoding         : 65001

 Date: 28/08/2025 19:54:13
*/


// ----------------------------
// Collection structure for menus
// ----------------------------
db.getCollection("menus").drop();
db.createCollection("menus");

// ----------------------------
// Documents of menus
// ----------------------------
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cf5"),
    menu_id: NumberLong("1"),
    component: null,
    icon: "user",
    name: "用户管理",
    parent_id: NumberLong("0"),
    path: "/eugene/users.html",
    sort: NumberInt("1"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("1")
} ]);
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cf6"),
    menu_id: NumberLong("2"),
    component: null,
    icon: "lock",
    name: "权限管理",
    parent_id: NumberLong("0"),
    path: "/eugene/roles.html",
    sort: NumberInt("2"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("1")
} ]);
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cf7"),
    menu_id: NumberLong("3"),
    component: null,
    icon: "log",
    name: "菜单管理",
    parent_id: NumberLong("0"),
    path: "/eugene/menus.html",
    sort: NumberInt("3"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("0")
} ]);
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cf8"),
    menu_id: NumberLong("4"),
    component: null,
    icon: "logs",
    name: "日志管理",
    parent_id: NumberLong("0"),
    path: "/eugene/logs.html",
    sort: NumberInt("0"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("1")
} ]);
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cf9"),
    menu_id: NumberLong("5"),
    component: null,
    icon: "err_log",
    name: "错误日志",
    parent_id: NumberLong("4"),
    path: "/eugene/err_log.html",
    sort: NumberInt("0"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("1")
} ]);
db.getCollection("menus").insert([ {
    _id: ObjectId("68b050a264e1ee7fb0759cfa"),
    menu_id: NumberLong("6"),
    component: null,
    icon: "sys_log",
    name: "系统日志",
    parent_id: NumberLong("4"),
    path: "/eugene/sys_log.html",
    sort: NumberInt("0"),
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:50:42.841Z"),
    visible: NumberInt("1")
} ]);

// ----------------------------
// Collection structure for users
// ----------------------------
db.getCollection("users").drop();
db.createCollection("users",{
    validator: {
        $jsonSchema: {
            bsonType: "object",
            required: [
                "user_id",
                "site_id",
                "username",
                "password_hash",
                "status",
                "updated_at"
            ],
            properties: {
                user_id: {
                    bsonType: "long"
                },
                site_id: {
                    bsonType: "long"
                },
                username: {
                    bsonType: "string",
                    minLength: 1
                },
                password_hash: {
                    bsonType: "string",
                    minLength: 1
                },
                status: {
                    bsonType: "int"
                },
                last_login_at: {
                    bsonType: [
                        "date",
                        "null"
                    ]
                },
                updated_at: {
                    bsonType: "date"
                }
            }
        }
    },
    validationLevel: "moderate"
});
db.getCollection("users").createIndex({
    username: NumberInt("1")
}, {
    name: "username_1",
    unique: true
});
db.getCollection("users").createIndex({
    user_id: NumberInt("1")
}, {
    name: "user_id_1",
    unique: true
});

// ----------------------------
// Documents of users
// ----------------------------
db.getCollection("users").insert([ {
    _id: ObjectId("68b025d21a356ac56408d5d2"),
    user_id: NumberLong("1"),
    site_id: NumberLong("10001"),
    username: "eugene",
    password_hash: "$2a$10$vRDD/4zpG/DOpiA6sZRdWuwHfuAN6LjYcAr2v9FUcCSsNo0/XmPIe",
    status: NumberInt("1"),
    updated_at: ISODate("2025-08-28T12:45:49.576Z"),
    last_login_at: null
} ]);
