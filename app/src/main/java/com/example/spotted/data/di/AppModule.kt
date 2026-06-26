package com.example.spotted.data.di

import com.example.spotted.data.repository.*
import com.example.spotted.data.view.*
import com.example.spotted.viewmodel.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Repository (tutti singleton)
    single { UserRepository() }
    single { CategoryRepository() }
    single { PostRepository() }
    single { FollowRepository() }
    single { DiscardedRepository() }
    single { MessageRepository() }
    single { StorageRepository() }

    // ViewModel (factory)
    viewModel { ProfileViewModel(get()) }
    viewModel { ShareViewModel(get(), get(), get()) }
    viewModel { ChatViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignupViewModel(get()) }
    viewModel { FollowingViewModel(get(), get(), get()) }
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { MapViewModel(get()) }
}