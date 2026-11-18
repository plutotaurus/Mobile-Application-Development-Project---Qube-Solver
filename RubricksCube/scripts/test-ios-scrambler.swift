// Run from RubricksCube: swift scripts/test-ios-scrambler.swift
// Uses the same Apple JavaScriptCore runtime as the iPhone implementation.
import Foundation
import JavaScriptCore

let root = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
    .deletingLastPathComponent()
    .appendingPathComponent("shared/src/commonMain/composeResources/files")
let context = JSContext()!
func evaluate(_ script: String) -> JSValue {
    context.exception = nil
    let value = context.evaluateScript(script)!
    precondition(context.exception == nil, context.exception?.toString() ?? "JavaScript failed")
    return value
}
for name in ["min2phase.js", "scramble3x3.js"] {
    _ = evaluate(try String(contentsOf: root.appendingPathComponent(name), encoding: .utf8))
}
let count = evaluate("""
(function () {
    var solved = 'UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB';
    for (var i = 0; i < 100; i++) {
        var scramble = qubeScrambler.generate();
        var state = min2phase.fromScramble(scramble);
        if (state === solved) throw new Error('Scramble was solved');
        var solution = min2phase.solve(state);
        if (min2phase.fromScramble(scramble + ' ' + solution) !== solved) {
            throw new Error('Scramble did not solve back to the original state');
        }
    }
    return 100;
})();
""").toInt32()
precondition(count == 100)
print("Passed: 100 scramble/solution round trips in Apple JavaScriptCore.")
