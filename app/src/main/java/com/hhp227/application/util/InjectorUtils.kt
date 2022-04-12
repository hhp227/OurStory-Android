package com.hhp227.application.util

import androidx.activity.ComponentActivity
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

    fun provideGroupViewModelFactory(): GroupViewModelFactory {
        return GroupViewModelFactory(getGroupRepository(), getPreferenceManager())
    }

    fun provideCreateGroupViewModelFactory(): CreateGroupViewModelFactory {
        return CreateGroupViewModelFactory(getGroupRepository(), getPreferenceManager())
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

    fun provideChatMessageViewModelFactory(activity: ComponentActivity): ChatMessageViewModelFactory {
        return ChatMessageViewModelFactory(getChatRepository(), getPreferenceManager(), activity, activity.intent.extras)
    }

    fun provideChatViewModelFactory(): ChatViewModelFactory {
        return ChatViewModelFactory(getChatRepository())
    }

    fun provideImageSelectViewModelFactory(): ImageSelectViewModelFactory {
        return ImageSelectViewModelFactory(getImageRepository())
    }

    fun provideCreatePostViewModelFactory(activity: ComponentActivity): CreatePostViewModelFactory {
        return CreatePostViewModelFactory(getPostRepository(), getPreferenceManager(), activity, activity.intent.extras)
    }

    fun providePostDetailViewModelFactory(activity: ComponentActivity): PostDetailViewModelFactory {
        return PostDetailViewModelFactory(getPostRepository(), getReplyRepository(), getPreferenceManager(), activity, activity.intent.extras)
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

    fun provideUpdateReplyViewModelFactory(activity: ComponentActivity): UpdateReplyViewModelFactory {
        return UpdateReplyViewModelFactory(ReplyRepository(), getPreferenceManager(), activity, activity.intent.extras)
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
        return MyInfoViewModelFactory(getUserRepository(), getPreferenceManager())
    }

    fun provideSplashViewModelFactory(): SplashViewModelFactory {
        return SplashViewModelFactory(getPreferenceManager())
    }
}