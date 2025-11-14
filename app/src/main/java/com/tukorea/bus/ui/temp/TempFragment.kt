package com.tukorea.bus.ui.temp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tukorea.bus.databinding.FragmentTempBinding
import com.tukorea.bus.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TempFragment : Fragment() {

    private var _binding: FragmentTempBinding? = null
    private val binding get() = _binding!!
    private val vm: TempViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTempBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLoad.setOnClickListener { vm.loadTemps() } // ViewModel 메서드 변경

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { res ->
                    when (res) {
                        is Resource.Loading -> binding.textResult.text = "로딩 중..."
                        is Resource.Success -> {
                            val tempsText = res.data.joinToString("\n") { it.temp }
                            binding.textResult.text = tempsText
                        }
                        is Resource.Error -> {
                            binding.textResult.text = "에러: ${res.message}"
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}