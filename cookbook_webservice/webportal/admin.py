from django.contrib import admin

# Register your models here.
from webportal.models import *

admin.site.register(Recipe)
admin.site.register(Tag)
admin.site.register(Unit)
admin.site.register(Ingredient)
admin.site.register(WorkStep)
admin.site.register(RecipeWorkStep)
admin.site.register(CookingSession)
