package com.example.spotted.data.di

import com.example.spotted.data.repository.*
import com.example.spotted.data.view.*
import com.example.spotted.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.*
import org.koin.dsl.module

val appModule = module {
    // Repository (tutti singleton)
    single { UserRepository() }
    single { CategoryRepository() }
    single { FollowRepository() }
    single { DiscardedRepository() }
    single { PostRepository(get(), get()) }
    single { MessageRepository() }
    single { StorageRepository() }

    viewModel { ProfileViewModel(get()) }
    viewModel { ShareViewModel(get(), get(), get()) }
    viewModel { ChatViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignupViewModel(get()) }
    viewModel { FollowingViewModel(get(), get(), get()) }
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { MapViewModel(get()) }
}