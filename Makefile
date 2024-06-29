init:
	git submodule update --init --recursive

compile:
	./mill -i -j 0 __.compile

clean:
	git clean -fd && rm -rf .idea out test_run_dir target project

idea:
	./mill -i -j 0 mill.idea.GenIdea/idea

test:
	./mill fpuv2[chisel3].test.testOnly FPUv2.FMATest
