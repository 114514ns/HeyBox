package cn.pprocket.items


class Post {
    var title = ""
    var postId = ""
    var userId = ""
    var userAvatar = ""
    var userName = ""
    var description = ""
    var images = mutableListOf<String>()
    var createAt = ""
    var comments = 0
    var likes = 0
    var content = ""
    var tags = mutableListOf<Topic>()
    var isHTML = false
}
