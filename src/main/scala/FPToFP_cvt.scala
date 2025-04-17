package FPUv2

import FPUv2.utils.FPUOps._
import FPUv2.utils._
import chisel3._
import fudian.{FPToFP, _}

class FPToFP_cvt(ctrlGen: Data = EmptyFPUCtrl())
  extends FPUPipelineModule(32, ctrlGen) {
  override def latency = 1

  val isFP16ToFP32 = io.in.bits.op === "b000".U
  val isFP32ToFP16 = io.in.bits.op === "b001".U

  val FP2FCore = (
    Module(new FPToFP(5, 11, 8, 24)),  // FP16 to FP32
    Module(new FPToFP(8, 24, 5, 11))   // FP32 to FP16
  )

  // Handle FP16 to FP32 conversion
  FP2FCore._1.io.in := io.in.bits.b
  FP2FCore._1.io.rm := io.in.bits.rm
//  FP2FCore._1.io <> DontCare

  // Handle FP32 to FP16 conversion
  FP2FCore._2.io.in := io.in.bits.b
  FP2FCore._2.io.rm := io.in.bits.rm
//  FP2FCore._2.io <> DontCare

  // Determine which conversion to output
  io.out.bits.result := Mux(isFP16ToFP32, S1Reg(FP2FCore._1.io.result),
    Mux(isFP32ToFP16, S1Reg(FP2FCore._2.io.result), 0.U(32.W)))
  io.out.bits.fflags := Mux(isFP16ToFP32, S1Reg(FP2FCore._1.io.fflags),
    Mux(isFP32ToFP16, S1Reg(FP2FCore._2.io.fflags), 0.U(5.W)))
  io.out.bits.ctrl.foreach(_ := S1Reg(io.in.bits.ctrl.get))
}

