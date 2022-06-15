package com.hhp227.application.util

import androidx.fragment.app.Fragment
import com.hhp227.application.app.AppController
import com.hhp227.application.data.*
import com.hhp227.application.viewmodel.*

object InjectorUtils {
    private fun getGroupRepository() = GroupRepository.getInstance()

    private fun getChatRepository() = ChatRepository.getInstance()

    private fun getImageRepository() = ImageRepository.getInstance()

    private fun getPostRepository() = PostRepository.getInstance()

    private fun getReplyRepository() = ReplyRepository.getInstance()

    private fun getUserRepository() = UserRepository.getInstance()

    private fun getPreferenceManager() = AppController.getInstance().preferenceManager

    private fun getPhotoUriManager() = AppController.getInstance().photoUriManager

    fun provideGroupViewModelFactory(): GroupViewModelFactory {
        return GroupViewModelFactory(getGroupRepository(), getPreferenceManager())
    }

    fun provideCreateGroupViewModelFactory(): CreateGroupViewModelFactory {
        return CreateGroupViewModelFactory(getGroupRepository(), getPhotoUriManager(), getPreferenceManager())
    }

    fun provideFindGroupViewModelFactory(): FindGroupViewModelFactory {
        return FindGroupViewModelFactory(getGroupRepository(), getPreferenceManager())
    }

    fun provideJoinRequestGroupViewModelFactory(): JoinRequestGroupViewModelFactory {
        return JoinRequestGroupViewModelFactory(getGroupRepository(), getPreferenceManager())
    }

    fun provideSettingsViewModelFactory(fragment: Fragment): SettingsViewModelFactory {
        return SettingsViewModelFactory(getGroupRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideGroupInfoViewModelFactory(fragment: Fragment): GroupInfoViewModelFactory {
        return GroupInfoViewModelFactory(getGroupRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideChatMessageViewModelFactory(fragment: Fragment): ChatMessageViewModelFactory {
        return ChatMessageViewModelFactory(getChatRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideChatViewModelFactory(): ChatViewModelFactory {
        return ChatViewModelFactory(getChatRepository())
    }

    fun provideImageSelectViewModelFactory(): ImageSelectViewModelFactory {
        return ImageSelectViewModelFactory(getImageRepository())
    }

    fun provideCreatePostViewModelFactory(fragment: Fragment): CreatePostViewModelFactory {
        return CreatePostViewModelFactory(getPostRepository(), getPhotoUriManager(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun providePostDetailViewModelFactory(fragment: Fragment): PostDetailViewModelFactory {
        return PostDetailViewModelFactory(getPostRepository(), getReplyRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideLoungeViewModelFactory(): LoungeViewModelFactory {
        return LoungeViewModelFactory(getPostRepository(), getPreferenceManager())
    }

    fun providePostViewModelFactory(fragment: Fragment): PostViewModelFactory {
        return PostViewModelFactory(getPostRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideAlbumViewModelFactory(fragment: Fragment): AlbumViewModelFactory {
        return AlbumViewModelFactory(getPostRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideMyPostViewModelFactory(): MyPostViewModelFactory {
        return MyPostViewModelFactory(getPostRepository(), getPreferenceManager())
    }

    fun provideUpdateReplyViewModelFactory(fragment: Fragment): UpdateReplyViewModelFactory {
        return UpdateReplyViewModelFactory(getReplyRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideLoginViewModelFactory(): LoginViewModelFactory {
        return LoginViewModelFactory(getUserRepository(), getPreferenceManager())
    }

    fun provideRegisterViewModelFactory(): RegisterViewModelFactory {
        return RegisterViewModelFactory(getUserRepository(), getPreferenceManager())
    }

    fun provideMemberViewModelFactory(fragment: Fragment): MemberViewModelFactory {
        return MemberViewModelFactory(getUserRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideMyInfoViewModelFactory(): MyInfoViewModelFactory {
        return MyInfoViewModelFactory(getUserRepository(), getPreferenceManager(), getPhotoUriManager())
    }

    fun provideSplashViewModelFactory(): SplashViewModelFactory {
        return SplashViewModelFactory(getPreferenceManager())
    }

    fun provideUserViewModelFactory(fragment: Fragment): UserViewModelFactory {
        return UserViewModelFactory(getUserRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideFriendViewModelFactory(): FriendViewModelFactory {
        return FriendViewModelFactory(getUserRepository(), getPreferenceManager())
    }
}