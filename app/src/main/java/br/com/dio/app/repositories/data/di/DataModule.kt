package br.com.dio.app.repositories.data.di

import android.util.Log
import br.com.dio.app.repositories.data.repositories.RepoRepository
import br.com.dio.app.repositories.data.repositories.RepoRepositoryImpl
import br.com.dio.app.repositories.data.service.GitHubService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DataModule {

    private const val OKHTTP = "okHttp"

    fun load() {
        loadKoinModules(networkModules() + repositoriesModule())
    }

    private fun networkModules(): Module{
        return module {
            // Single - Devolve a mesma instância ao invés de uma nova instância a cada chamada
            // Interceptor provê uma intereção / visualização do retorno da request
            // através de um log.
            single {
                val interceptor = HttpLoggingInterceptor {
                    Log.e(OKHTTP, it)
                }

                interceptor.level = HttpLoggingInterceptor.Level.BODY

                OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()
            }


            //Gson - serialização e desserialização de dados (tipo: Json) durante
            // conversa entre nosso cliente e api server (request and response)
            single {
                GsonConverterFactory.create(
                    GsonBuilder().create()
                )
            }

            // Cria o serviço passando a responsabilidade para o koin
            single {
                createService<GitHubService>(get(), get())
            }
        }
    }

    private fun repositoriesModule(): Module {
        return module {
            single<RepoRepository> { RepoRepositoryImpl(get()) }
        }
    }

    private inline fun <reified T> createService (client: OkHttpClient, factory: GsonConverterFactory): T {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(factory)
            .build().create(T::class.java)
    }

}