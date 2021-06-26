package com.hhp227.application.dto

data class GroupItem constructor(
    var id: Int = 0,
    var authorId: Int = 0,
    var groupName: String? = null,
    var authorName: String? = null,
    var image: String? = null,
    var description: String? = null,
    var createdAt: String? = null,
    var joinType: Int = 0
)