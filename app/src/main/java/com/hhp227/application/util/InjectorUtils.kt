package com.hhp227.application.util

import android.util.Log
import androidx.fragment.app.Fragment
import com.hhp227.application.api.*
import com.hhp227.application.app.AppController
import com.hhp227.application.data.*
import com.hhp227.application.fcm.FcmTopicSubscriber
import com.hhp227.application.viewmodel.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object InjectorUtils {
    private fun getGroupRepository() = GroupRepository.getInstance(GroupService.create(), GroupDao)

    private fun getChatRepository() = ChatRepository.getInstance(ChatService.create(), ChatDao)

    private fun getImageRepository() = ImageRepository.getInstance()

    private fun getPostRepository() = PostRepository.getInstance(PostService.create(), PostDao)

    private fun getReplyRepository() = ReplyRepository.getInstance(ReplyService.create())

    private fun getUserRepository() = UserRepository.getInstance(AuthService.create(), UserService.create(), UserDao)

    private fun getAlbumRepository() = AlbumRepository.getInstance(PostService.create(), AlbumDao)

    private fun getPreferenceManager() = AppController.getInstance().preferenceManager

    private fun getPhotoUriManager() = AppController.getInstance().photoUriManager

    fun provideTopicSubscriber(): FcmTopicSubscriber {
        return FcmTopicSubscriber()
    }

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
        return ChatViewModelFactory(getChatRepository(), provideTopicSubscriber())
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
        return AlbumViewModelFactory(getAlbumRepository(), getPreferenceManager(), fragment, fragment.arguments)
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
        return RegisterViewModelFactory(getUserRepository())
    }

    fun provideMemberViewModelFactory(fragment: Fragment): MemberViewModelFactory {
        return MemberViewModelFactory(getUserRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideMyInfoViewModelFactory(): MyInfoViewModelFactory {
        return MyInfoViewModelFactory(getUserRepository(), getPreferenceManager(), getPhotoUriManager())
    }

    fun provideMainViewModelFactory(): MainViewModelFactory {
        return MainViewModelFactory(getPreferenceManager())
    }

    fun provideUserViewModelFactory(fragment: Fragment): UserViewModelFactory {
        return UserViewModelFactory(getUserRepository(), getPreferenceManager(), fragment, fragment.arguments)
    }

    fun provideFriendViewModelFactory(): FriendViewModelFactory {
        return FriendViewModelFactory(getUserRepository(), getPreferenceManager())
    }

    fun providePictureViewModelFactory(fragment: Fragment): PictureViewModelFactory {
        return PictureViewModelFactory(fragment, fragment.arguments)
    }

    fun provideRetrofit(): Retrofit {
        val Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val logger = HttpLoggingInterceptor { Log.d("API", it) }
        logger.level = HttpLoggingInterceptor.Level.BASIC
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
        return Retrofit.Builder()
            .baseUrl(URLs.BASE_URL.plus("/").toHttpUrlOrNull()!!)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}