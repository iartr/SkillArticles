package ru.skillbranch.skillarticles.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.ui.dialogs.AvatarActionsDialog
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.profile.PendingAction
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment() : BaseFragment<ProfileViewModel>() {

    //for testing
    private lateinit var resultRegistry: ActivityResultRegistry
    var _mockFactory: ((SavedStateRegistryOwner) -> ViewModelProvider.Factory)? = null

    override val viewModel: ProfileViewModel by viewModels {
        _mockFactory?.invoke(this) ?: defaultViewModelProviderFactory
    }
    override val layout: Int = ru.skillbranch.skillarticles.R.layout.fragment_profile
    override val binding: ProfileBinding by lazy { ProfileBinding() }

    //testing constructor
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    constructor(
        mockRoot: RootActivity,
        testRegistry: ActivityResultRegistry? = null,
        mockFactory: ((SavedStateRegistryOwner) -> ViewModelProvider.Factory)? = null
    ) : this() {
        _mockRoot = mockRoot
        _mockFactory = mockFactory
        if (testRegistry != null) resultRegistry = testRegistry
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var permissionsLauncher: ActivityResultLauncher<Array<out String>>

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var galleryLauncher: ActivityResultLauncher<String>

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var editPhotoLauncher: ActivityResultLauncher<Pair<Uri, Uri>>

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var settingsLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (!::resultRegistry.isInitialized) resultRegistry = requireActivity().activityResultRegistry

        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),resultRegistry,::callbackPermissions)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture(), resultRegistry, ::callbackCamera)
        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent(), resultRegistry, ::callbackGallery)
        editPhotoLauncher = registerForActivityResult(EditImageContract(), resultRegistry, ::callbackEditPhoto)
        settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), resultRegistry, ::callbackSettings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //listen fragment result
        setFragmentResultListener(AvatarActionsDialog.AVATAR_ACTIONS_KEY) { _, bundle ->
            when (bundle[AvatarActionsDialog.SELECT_ACTION_KEY] as String) {
                AvatarActionsDialog.CAMERA_KEY -> viewModel.handleCameraAction(prepareTempUri())
                AvatarActionsDialog.GALLERY_KEY -> viewModel.handleGalleryAction()
                AvatarActionsDialog.DELETE_KEY -> viewModel.handleDeleteAction()
                AvatarActionsDialog.EDIT_KEY -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        //Glide submit get it is sync call, don`t call on UI thread
                        val sourceFile = Glide.with(requireActivity())
                            .asFile()
                            .load(binding.avatar)
                            .submit()
                            .get()
                        val sourceUri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.provider",
                            sourceFile
                        )

                        withContext(Dispatchers.Main) {
                            viewModel.handleEditAction(sourceUri, prepareTempUri())
                        }
                    }
                }
            }

        }
    }

    override fun setupViews() {
        iv_avatar.setOnClickListener {
            val action = ProfileFragmentDirections.actionNavProfileToAvatarActionsDialog(binding.avatar.isNotBlank())
            viewModel.navigate(NavigationCommand.To(action.actionId, action.arguments))
        }

        viewModel.observerPermissions(viewLifecycleOwner) {
            //launch callback for request permissions
            permissionsLauncher.launch(it.toTypedArray())
        }

        viewModel.observeActivityResults(viewLifecycleOwner) {
            when (it) {
                is PendingAction.GalleryAction -> galleryLauncher.launch(it.payload)
                is PendingAction.SettingsAction -> settingsLauncher.launch(it.payload)
                is PendingAction.CameraAction -> cameraLauncher.launch(it.payload)
                is PendingAction.EditAction -> editPhotoLauncher.launch(it.payload)
            }
        }
    }

    private fun updateAvatar(avatarUrl: String) {
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_avatar)
            .error(R.drawable.ic_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_avatar)
    }

    @VisibleForTesting
    fun prepareTempUri(): Uri {
        val timestamp = SimpleDateFormat("HHmmss").format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //create empty temp file with unique name
        val tempFile = File.createTempFile(
            "JPEG_${timestamp}",
            ".jpg",
            storageDir
        )

        //must return content: uri not file: uri
        val contentUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tempFile
        )

        Log.e("ProfileFragment", "file uri: ${tempFile.toUri()} content uri: $contentUri");

        return contentUri
    }

    @VisibleForTesting
    fun removeTempUri(uri: Uri?) {
        uri ?: return
        requireContext().contentResolver.delete(uri, null, null)
    }

    private fun callbackPermissions(result: Map<String, Boolean>) {
        val permissionsResult = result.mapValues { (permission, isGranted) ->
            if (isGranted) true to true
            else false to ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                permission
            )
        }
        //remove tempt file by uri if permissions denied
        val isAllGranted = !permissionsResult.values.map { it.first }.contains(false)
        if (!isAllGranted) {
            val tempUri = when (val pendingAction = binding.pendingAction) {
                is PendingAction.CameraAction -> pendingAction.payload
                is PendingAction.EditAction -> pendingAction.payload.second
                else -> null
            }
            removeTempUri(tempUri)
        }

        viewModel.handlePermission(permissionsResult)
    }

    private fun callbackCamera(result: Boolean) {
        val (payload) = binding.pendingAction as PendingAction.CameraAction
        //if take photo from camera upload to server
        if (result) {
            val inputStream = requireContext().contentResolver.openInputStream(payload)
            viewModel.handleUploadPhoto(inputStream)
        } else {
            //else remove temp uri
            removeTempUri(payload)
        }
    }

    private fun callbackGallery(result: Uri?) {
        if (result != null) {
            val inputStream = requireContext().contentResolver.openInputStream(result)
            viewModel.handleUploadPhoto(inputStream)
        }
    }

    private fun callbackEditPhoto(result: Uri?) {
        if (result != null) {
            val inputStream = requireContext().contentResolver.openInputStream(result)
            viewModel.handleUploadPhoto(inputStream)
        } else {
            //else remove temp uri
            val (payload) = binding.pendingAction as PendingAction.EditAction
            removeTempUri(payload.second)
        }
    }

    private fun callbackSettings(result: ActivityResult) {
        //TODO do something
    }

    inner class ProfileBinding : Binding() {
        var pendingAction: PendingAction? = null

        var avatar by RenderProp("") {
            updateAvatar(it)
        }

        var name by RenderProp("") {
            tv_name.text = it
        }

        var about by RenderProp("") {
            tv_about.text = it
        }

        var rating by RenderProp(0) {
            tv_rating.text = "Rating: $it"
        }

        var respect by RenderProp(0) {
            tv_respect.text = "Respect: $it"
        }

        override fun bind(data: IViewModelState) {
            data as ProfileState
            if (data.avatar != null) avatar = data.avatar
            if (data.name != null) name = data.name
            if (data.about != null) about = data.about
            rating = data.rating
            respect = data.respect
            pendingAction = data.pendingAction
        }
    }

}
