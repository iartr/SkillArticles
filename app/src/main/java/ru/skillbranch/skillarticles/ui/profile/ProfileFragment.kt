package ru.skillbranch.skillarticles.ui.profile

import androidx.fragment.app.viewModels
import kotlinx.android.synthetic.main.fragment_profile.*
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModels()
    override val layout: Int = ru.skillbranch.skillarticles.R.layout.fragment_profile
    override val binding: ProfileBinding by lazy { ProfileBinding() }

    override fun setupViews() {

    }

    private fun updateAvatar(avatarUrl:String){
        //TODO update avatar with Glide this
    }

    inner class ProfileBinding: Binding() {
        var avatar by RenderProp(""){
            updateAvatar(it)
        }

        var name by RenderProp(""){
            tv_name.text = it
        }

        var about by RenderProp(""){
            tv_about.text = it
        }

        var rating by RenderProp(0){
            tv_rating.text = "Rating: $it"
        }

        var respect by RenderProp(0){
            tv_respect.text = "Respect: $it"
        }

        override fun bind(data: IViewModelState) {
            data as ProfileState
            if(data.avatar!=null) avatar = data.avatar
            if(data.name!=null) name = data.name
            if(data.about!=null) about = data.about
            rating = data.rating
            respect = data.respect
        }
    }

}
