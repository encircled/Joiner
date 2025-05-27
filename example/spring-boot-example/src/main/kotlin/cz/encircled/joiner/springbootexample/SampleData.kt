package cz.encircled.joiner.springbootexample

import cz.encircled.joiner.core.Joiner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SampleData : ApplicationRunner  {

    @Autowired
    @Lazy
    lateinit var joiner: Joiner

    @Transactional
    override fun run(args: ApplicationArguments?) {
        repeat(10) {
            val entity = Person("Person $it")
            entity.id = it.toLong()
            val employment = Employment("Employment $it", 1000.0)
            employment.id = it.toLong()
            employment.person = entity
            entity.employments.add(employment)
            joiner.save(entity)
        }
    }

}