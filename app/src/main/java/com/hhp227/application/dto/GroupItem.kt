package com.hhp227.application.dto

/*sealed class GroupTemp {
    object Title : GroupTemp()
    object Ad : GroupTemp()
    data class GroupItem constructor(
        var id: Int = 0,
        var authorId: Int = 0,
        var groupName: String? = null,
        var authorName: String? = null,
        var image: String? = null,
        var description: String? = null,
        var createdAt: String? = null,
        var joinType: Int = 0
    ) : GroupTemp()
}*/

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

class AdItem(val text: String)