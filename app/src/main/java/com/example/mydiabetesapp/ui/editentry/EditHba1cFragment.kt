package com.example.mydiabetesapp.ui.editentry

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mydiabetesapp.data.database.*
import com.example.mydiabetesapp.databinding.FragmentEditHba1cBinding
import com.example.mydiabetesapp.repository.Hba1cRepository
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditHba1cFragment : Fragment() {

    private var _b: FragmentEditHba1cBinding? = null
    private val b get() = _b!!
    private val args: EditHba1cFragmentArgs by navArgs()

    private lateinit var vm: Hba1cViewModel
    private val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(i: LayoutInflater,c: ViewGroup?,s: Bundle?): View {
        _b = FragmentEditHba1cBinding.inflate(i,c,false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {

        b.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        vm = ViewModelProvider(
            this,
            Hba1cViewModelFactory(Hba1cRepository(AppDatabase.getDatabase(requireContext()).hba1cDao()))
        )[Hba1cViewModel::class.java]

        lifecycleScope.launch {
            vm.get(args.entryId)?.let { fill(it) }
        }

        b.containerDate.setOnClickListener { datePicker() }

        b.btnSave.setOnClickListener {
            val value = b.etHba1c.text.toString().toFloatOrNull()
            if (value==null){ toast("Введите значение"); return@setOnClickListener }
            vm.update(
                Hba1cEntry(
                    id = args.entryId,
                    userId = 1,
                    date = b.tvDate.text.toString(),
                    hba1c = value
                )
            )
            toast("Запись обновлена"); findNavController().navigateUp()
        }
    }

    private fun fill(e:Hba1cEntry){
        b.tvDate.text = e.date
        b.etHba1c.setText("%.2f".format(e.hba1c))
    }

    private fun datePicker(){
        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(),{_,y,m,d->
            b.tvDate.text = String.format("%02d.%02d.%d",d,m+1,y)
        },now[Calendar.YEAR],now[Calendar.MONTH],now[Calendar.DAY_OF_MONTH]).show()
    }

    private fun toast(t:String)=Toast.makeText(requireContext(),t,Toast.LENGTH_SHORT).show()
    override fun onDestroyView(){ super.onDestroyView(); _b=null }
}
