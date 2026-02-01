package com.hhp227.application.util

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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

    fun provideGroupViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                GroupViewModel(getGroupRepository(), getPreferenceManager())
            }
        }
    }

    fun provideCreateGroupViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                CreateGroupViewModel(getGroupRepository(), getPhotoUriManager(), getPreferenceManager())
            }
        }
    }

    fun provideFindGroupViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                FindGroupViewModel(getGroupRepository(), getPreferenceManager())
            }
        }
    }

    fun provideJoinRequestGroupViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                JoinRequestGroupViewModel(getGroupRepository(), getPreferenceManager())
            }
        }
    }

    fun provideSettingsViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                SettingsViewModel(getGroupRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideGroupInfoViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                GroupInfoViewModel(getGroupRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideChatMessageViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                ChatMessageViewModel(getChatRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideChatViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                ChatViewModel(getChatRepository(), provideTopicSubscriber())
            }
        }
    }

    fun provideImageSelectViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                ImageSelectViewModel(getImageRepository(), AppController.getInstance())
            }
        }
    }

    fun provideCreatePostViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                CreatePostViewModel(getPostRepository(), getPhotoUriManager(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun providePostDetailViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PostDetailViewModel(getPostRepository(), getReplyRepository(), savedStateHandle, getPreferenceManager())
            }
        }
    }

    fun provideLoungeViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                LoungeViewModel(getPostRepository(), getPreferenceManager())
            }
        }
    }

    fun providePostViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PostViewModel(getPostRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideAlbumViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                AlbumViewModel(getAlbumRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideMyPostViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                MyPostViewModel(getPostRepository(), getPreferenceManager())
            }
        }
    }

    fun provideUpdateReplyViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                UpdateReplyViewModel(getReplyRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideLoginViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                LoginViewModel(getUserRepository(), getPreferenceManager())
            }
        }
    }

    fun provideRegisterViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                RegisterViewModel(getUserRepository())
            }
        }
    }

    fun provideMemberViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                MemberViewModel(getUserRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideMyInfoViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                MyInfoViewModel(getUserRepository(), getPreferenceManager(), getPhotoUriManager())
            }
        }
    }

    fun provideMainViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                MainViewModel(getPreferenceManager())
            }
        }
    }

    fun provideUserViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                UserViewModel(getUserRepository(), getPreferenceManager(), savedStateHandle)
            }
        }
    }

    fun provideFriendViewModelFactory(): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                FriendViewModel(getUserRepository(), getPreferenceManager())
            }
        }
    }

    fun providePictureViewModelFactory(fragment: Fragment): ViewModelProvider.Factory {
        return viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                PictureViewModel(savedStateHandle)
            }
        }
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