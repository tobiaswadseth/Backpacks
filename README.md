# Backpacks
#### A plugin for spigot that allows for storage on the go

Edit the config.yml to add custom backpacks
````yml
backpacks: 
  small: # Backpack Identifier
    name: "&cSmall &6Backpack" # Backpack name
    size: 9 # Backpack size, must be a multiple of 9 and max 54
    texture: "8351e505989838e27287e7afbc7f97e796cab5f3598a76160c131c940d0c5" # The custom texture of the backpack as the plugin uses player heads as the item
````

There is a single command:
```
/backpack [targetUser] [backpackIdentifier]
```